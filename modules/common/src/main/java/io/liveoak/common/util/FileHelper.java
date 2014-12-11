package io.liveoak.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.function.Function;

/**
 * @author Ken Finnigan
 */
public final class FileHelper {
    private FileHelper() {
    }

    public static void deleteNonEmpty(File directory) throws IOException {
        if (directory.exists()) {
            final Path directoryPath = directory.toPath();
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

    public static void copy(File from, File to, final boolean recursive, Function<Path, Boolean> excluded) throws IOException {
        final Path source = from.toPath();
        final Path target = to.toPath();

        if (recursive) {
            Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            Path newDir = target.resolve(source.relativize(dir));
                            if (excluded.apply(newDir)) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }

                            try {
                                Files.copy(dir, newDir);
                            } catch(FileAlreadyExistsException e) {
                                if (!Files.isDirectory(newDir)) {
                                    throw e;
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.copy(file, target.resolve(source.relativize(file)));
                            return FileVisitResult.CONTINUE;
                        }
                    }
            );
        } else {
            Files.copy(source, target);
        }
    }
}
