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

public class ChroniDump {

    private final ChronicleQueue cq;
    private final Codec codec;
    private final Bytes<ByteBuffer> sourceBytes = Bytes.elasticByteBuffer();
    private final Bytes<ByteBuffer> destBytes = Bytes.elasticByteBuffer();
    private final Connection conn;
    private long counter;

    public static void main(String[] args) throws SQLException {
        ChronicleQueue cq = ChronicleArgs.createChronicleQueue(args);
        Path directory = Paths.get(cq.fileAbsolutePath());
        addInfo(String.format("Reading from path %s", directory));
        CodecRegistry registry = CodecRegistry.builder().withDefaults(directory).build();
        // XXX Pull this from a configuration file
        Codec codec = registry.find(CodecRegistry.ZSTANDARD);

        String databaseName = "dump.db";
        try (Connection conn = createDatabaseConnection(databaseName);) {
            ChroniDump chroniDump = new ChroniDump(cq, codec, conn);
            chroniDump.dump();
        }
    }

    public static Connection createDatabaseConnection(String databaseName) throws SQLException {
        // https://github.com/xerial/sqlite-jdbc/blob/master/Usage.md#configure-connections
        // https://phiresky.github.io/blog/2020/sqlite-performance-tuning/
        SQLiteConfig config = new SQLiteConfig();
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        config.setTempStore(SQLiteConfig.TempStore.MEMORY);
        config.setPragma(SQLiteConfig.Pragma.MMAP_SIZE, "30000000000");

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseName, config.toProperties());
        connection.setAutoCommit(false);
        return connection;
    }

    public ChroniDump(ChronicleQueue cq, Codec codec, Connection conn) {
        this.cq = cq;
        this.codec = codec;
        this.conn = conn;
    }

    public void dump() throws SQLException {
        createTable();

        EntryReader reader = new EntryReader();
        ChronicleLogProcessor logProcessor = this::insert;
        logProcessor.processLogs(cq, reader, false);
    }

    protected void createTable() throws SQLException {
        String createStatements = getCreateStatements();
        addInfo("createTable: " + createStatements);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createStatements);
        }
        conn.commit();
    }

    private String getCreateStatements() {
        return "CREATE TABLE IF NOT EXISTS entries (" +
                        "ts TIMESTAMP NOT NULL," +
                        "level LONG NOT NULL," +
                        "logger_name VARCHAR(255) NOT NULL," +
                        "thread_name VARCHAR(255) NULL," +
                        "content BLOB NOT NULL)";
    }

    private String getInsertStatement() {
        return "INSERT INTO entries(ts, level, logger_name, thread_name, content)\n" +
                "values(?, ?, ?, ?, ?)";
    }

    public void insert(Entry e) {
        try {
            ByteBuffer byteBuffer = e.contentAsByteBuffer();
            sourceBytes.writeSome(byteBuffer);
            codec.decompress(sourceBytes, destBytes);

            String insertStatement = getInsertStatement();
            try (PreparedStatement statement = conn.prepareStatement(insertStatement)) {
                LongAdder adder = new LongAdder();
                adder.increment();
                counter = counter + 1;
                insertTimestamp(e.timestamp(), adder, statement);
                insertLevel(e.level(), adder, statement);
                insertLoggerName(e.loggerName(), adder, statement);
                insertThreadName(e.threadName(), adder, statement);
                insertContent(destBytes, adder, statement);
                statement.executeUpdate();
            }

            // Improve performance by batching
            // But this is from 2017, so things may have improved since then.
            // https://medium.com/@JasonWyatt/squeezing-performance-from-sqlite-insertions-971aff98eef2
            if (counter % 1000 == 0) {
                counter = 0;
                conn.commit();
            }
        } catch (SQLException ex) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(ex.getMessage());
            throw new IllegalStateException(ex);
        } finally {
            sourceBytes.clear();
            destBytes.clear();
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

    protected void insertContent(Bytes<?> bytes, LongAdder adder, PreparedStatement statement)
            throws SQLException {
        statement.setBinaryStream(adder.intValue(), bytes.inputStream(), bytes.length());
        adder.increment();
    }

    protected void insertTimestamp(EntryTimestamp ts, LongAdder adder, PreparedStatement statement)
            throws SQLException {
        Instant instant = EntryHelpers.instance().instantFromTimestamp(ts);
        statement.setString(adder.intValue(), instant.toString());
        adder.increment();
    }

    private static void addInfo(String s) {
        System.out.println("[INFO] " + s);
    }

    private static void addError(String error, Exception e) {
        System.err.println("[ERROR] " + error);
        e.printStackTrace();
    }

    private static void addError(String s) {
        System.err.println("[ERROR] " + s);
    }

    private static void addWarn(String s) {
        System.err.println("[WARN] " + s);
    }

    private static void addWarn(String s, SQLException e) {
        System.err.println("[WARN] " + s);
        e.printStackTrace();
    }
}
