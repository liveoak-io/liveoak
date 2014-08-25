package io.liveoak.container.zero;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

/**
 * @author Ken Finnigan
 */
public class AppCleanup implements Consumer<String> {

    void deleteNonEmptyDir(File dir) throws IOException {
        Path directory = dir.toPath();
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void accept(String path) {
        URL appUrl = AppCleanup.class.getClassLoader().getResource(path);
        if (appUrl != null) {
            File appDir = new File(appUrl.getFile());
            if (appDir != null && appDir.exists()) {
                try {
                    deleteNonEmptyDir(appDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
