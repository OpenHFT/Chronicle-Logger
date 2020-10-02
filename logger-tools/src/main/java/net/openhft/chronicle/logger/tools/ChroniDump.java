package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.logger.entry.Entry;
import net.openhft.chronicle.logger.entry.EntryHelpers;
import net.openhft.chronicle.logger.entry.EntryReader;
import net.openhft.chronicle.logger.entry.EntryTimestamp;
import net.openhft.chronicle.queue.ChronicleQueue;
import org.sqlite.SQLiteConfig;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;

import static java.util.Objects.requireNonNull;

public class ChroniDump {

    private final ChronicleQueue cq;
    private final Codec codec;
    private final Bytes<ByteBuffer> sourceBytes = Bytes.elasticByteBuffer();
    private final Bytes<ByteBuffer> destBytes = Bytes.elasticByteBuffer();
    private final Connection conn;

    public static void main(String[] args) throws SQLException {
        ChronicleQueue cq = ChronicleArgs.createChronicleQueue(args);
        Path parent = Paths.get(cq.fileAbsolutePath()).getParent();
        CodecRegistry registry = CodecRegistry.builder().withDefaults(parent).build();
        Codec codec = registry.find(CodecRegistry.ZSTANDARD);

        try (Connection conn = createDatabaseConnection();){
            ChroniDump chroniDump = new ChroniDump(cq, codec, conn);
            chroniDump.dump();
        }
    }

    public static Connection createDatabaseConnection() throws SQLException {
        // https://github.com/xerial/sqlite-jdbc/blob/master/Usage.md#configure-connections
        // https://phiresky.github.io/blog/2020/sqlite-performance-tuning/
        SQLiteConfig sqLiteConfig = new SQLiteConfig();
        sqLiteConfig.setJournalMode(SQLiteConfig.JournalMode.WAL);
        sqLiteConfig.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        sqLiteConfig.setTempStore(SQLiteConfig.TempStore.MEMORY);
        sqLiteConfig.setPragma(SQLiteConfig.Pragma.MMAP_SIZE, "30000000000");

        return DriverManager.getConnection("jdbc:sqlite:sample.db");
    }

    public ChroniDump(ChronicleQueue cq, Codec codec, Connection conn) {
        this.cq = cq;
        this.codec = codec;
        this.conn = conn;
    }

    public void dump() throws SQLException {
        EntryReader reader = new EntryReader();

        PreparedStatement ps = conn.prepareStatement("INSERT INTO ");

        ChronicleLogProcessor logProcessor = this::insert;
        logProcessor.processLogs(cq, reader, false);
    }

    protected void createTableIfNecessary() {
        // Initialize with DDL
        // XXX should really check if the table exists already
        String createStatements = getCreateStatements();
        if (createStatements == null || createStatements.trim().isEmpty()) {
            return;
        }

        addInfo("createTable: " + createStatements);
        try {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(createStatements);
            }
            conn.commit();
        } catch (SQLException e) {
            addWarn("Cannot create table, assuming it exists already", e);
        }
    }

    private String getCreateStatements() {
        return config.getString("chronicle.createStatements");
    }

    private String getInsertStatement() {
        return config.getString("chronicle.insertStatement");
    }

    public void insert(Entry e) {
        ByteBuffer byteBuffer = e.contentAsByteBuffer();
        sourceBytes.writeSome(byteBuffer);
        //System.out.println("read: " + sourceBytes.toHexString());
        int actualSize = codec.decompress(sourceBytes, destBytes);
        byte[] actualArray = new byte[actualSize];
        destBytes.write(actualArray);
        // Could get better performance by not wrapping inserts in individual transactions
        // But this is from 2017, so things may have improved since then.
        // https://medium.com/@JasonWyatt/squeezing-performance-from-sqlite-insertions-971aff98eef2
        try {
           String insertStatement = requireNonNull(getInsertStatement());
            try (PreparedStatement statement = conn.prepareStatement(insertStatement)) {
                LongAdder adder = new LongAdder();
                adder.increment();
                insertTimestamp(e.timestamp(), adder, statement);
                insertLevel(e.level(), adder, statement);
                insertLoggerName(e.loggerName(), adder, statement);
                insertThreadName(e.threadName(), adder, statement);
                //SmileToJson smileToJson = SmileToJson.instance();
                insertContent(actualArray, adder, statement);
                int result = statement.executeUpdate();
                addInfo("added " + result);
            }
        } catch (SQLException ex) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(ex.getMessage());
        }
    }

    private void insertLoggerName(String loggerName, LongAdder adder, PreparedStatement statement) throws SQLException {
        statement.setString(adder.intValue(), loggerName);
        adder.increment();
    }

    private void insertThreadName(String threadName, LongAdder adder, PreparedStatement statement) throws SQLException {
        statement.setString(adder.intValue(), threadName);
        adder.increment();
    }

    protected void insertLevel(long level, LongAdder adder, PreparedStatement statement) throws SQLException {
        statement.setLong(adder.intValue(), level);
        adder.increment();
    }

    protected void insertContent(byte[] content, LongAdder adder, PreparedStatement statement)
            throws SQLException {
        statement.setBytes(adder.intValue(), content);
        adder.increment();
    }

    protected void insertTimestamp(EntryTimestamp ts, LongAdder adder, PreparedStatement statement)
            throws SQLException {
        Instant instant = EntryHelpers.instance().instantFromTimestamp(ts);
        statement.setString(adder.intValue(), instant.toString());
        adder.increment();
    }

    private void addInfo(String s) {
        System.out.println("[INFO] " + s);
    }

    private void addError(String error, Exception e) {
        System.err.println("[ERROR] " + error);
        e.printStackTrace();
    }

    private void addError(String s) {
        System.err.println("[ERROR] " + s);
    }

    private void addWarn(String s) {
        System.err.println("[WARN] " + s);
    }

    private void addWarn(String s, SQLException e) {
        System.err.println("[WARN] " + s);
        e.printStackTrace();
    }
}
