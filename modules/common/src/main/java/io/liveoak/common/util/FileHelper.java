package io.liveoak.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Ken Finnigan
 */
public final class FileHelper {
    private FileHelper() {
    }

    public static void deleteNonEmpty(File directory) throws IOException {
        Path directoryPath = directory.toPath();
        Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!file.toFile().delete()) {
                    throw new IOException("Failed to delete file: " + file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                java.nio.file.Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
