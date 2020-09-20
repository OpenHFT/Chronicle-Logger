package net.openhft.chronicle.logger;

import net.openhft.chronicle.queue.impl.StoreFileListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Only keeps the last few CQ4 files.
 */
public class ChronicleLogRemover implements StoreFileListener {

    private final int limit;

    public ChronicleLogRemover(int limit) {
        this.limit = limit;
    }

    public ChronicleLogRemover() {
        this.limit = 5;
    }

    public static long getFileCreationEpoch(File file) {
        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(),
                    BasicFileAttributes.class);
            return attr.creationTime()
                    .toInstant().toEpochMilli();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<Path> getSortedFilesByDateCreated(Path parentFolder) {
        try {
            Comparator<Path> pathComparator = Comparator.comparingLong(p -> getFileCreationEpoch((p).toFile()));
            return Files.list(parentFolder)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith("cq4"))
                    .sorted(pathComparator.reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // XXX Should probably add a size concern in here as well, and check the total disk free
    // on the filesystem.
    protected void deleteOldFiles(Path parentFolder, int limit) {
        List<Path> files = getSortedFilesByDateCreated(parentFolder);
        if (files.size() <= limit) {
            return;
        }
        //delete recent files and keeping old files in the list
        files.subList(0, limit).clear();

        //deleting old files
        files.forEach(p -> {
            try {
                Files.delete(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void checkFileSystem() throws IOException {
        for (FileStore store : FileSystems.getDefault().getFileStores()) {
            long total = store.getTotalSpace() / 1024;
            long used = (store.getTotalSpace() - store.getUnallocatedSpace()) / 1024;
            long avail = store.getUsableSpace() / 1024;
            System.out.format("%-20s %12d %12d %12d%n", store, total, used, avail);
        }
    }

    @Override
    public void onReleased(int cycle, File file) {
        Path parentFolder = file.getParentFile().toPath();
        deleteOldFiles(parentFolder, limit);
    }
}
