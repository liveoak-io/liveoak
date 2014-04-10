/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.launcher;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MongoInstaller {

    private static final String CLEAR;

    static {
        char [] buf = new char[80];
        Arrays.fill(buf, '\b');
        CLEAR = new String(buf);
    }

    private static final String URL_ROOT = "http://fastdl.mongodb.org";

    private String version = "2.6.0";
    private String tempDir;
    private String installDir;
    private File mongod;

    private int lastProgress = -1;

    public void setVersion(String ver) {
        this.version = ver;
    }

    public void setTempDir(String dir) {
        tempDir = dir;
    }

    public void setInstallDir(String dir) {
        installDir = dir;
    }

    public String getMongodPath() {
        return mongod.getAbsolutePath();
    }

    public void performInstall() throws IOException {
        System.err.println( "attempting to install mongo!" );
        System.err.println( "attempting to install mongo!" );
        System.err.println( "attempting to install mongo!" );
        System.err.println( "attempting to install mongo!" );
        System.err.println( "attempting to install mongo!" );
        // determine architecture
        OsArch osArch = OsUtils.determineOSAndArch();

        // compose download url
        String fileExt = osArch.isWindows() ? ".zip" : ".tgz";
        String fileName = "mongodb-" + osArch.os() + "-" + osArch.arch()
                + (osArch.isWindows() && osArch.isLegacy() ? "-2008plus-" : "-")
                + version;

        String url = URL_ROOT + "/" + osArch.os() + "/" + fileName + fileExt;

        // download into temp dir
        if (tempDir == null) {
            tempDir = System.getProperty("java.io.tmpdir");
        }

        File dest = new File(tempDir, fileName + fileExt);

        if (installDir == null) {
            installDir = System.getProperty("user.dir");
        }
        // check if there is already an installation of the same version in install dir
        File [] matches = new File(installDir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().equals(fileName);
            }
        });

        boolean skipDownload = false;
        if (matches.length == 1) {
            if (matches[0].isDirectory()) {
                System.out.println("MongoDB already installed at: " + matches[0].getAbsolutePath());
                skipDownload = true;
            } else {
                throw new RuntimeException("Installation can't continue. Please remove the file: " + matches[0].getAbsolutePath());
            }
        }

        if (!skipDownload) {
            System.out.println("No existing MongoDB found");
            System.out.println("Installing from: " + url);

            download(new URL(url), dest);
            //fakeDownload(new URL(url), dest);

            try {
                // unpack into install location
                System.out.println("Unpacking to: " + installDir);
                if (osArch.isWindows()) {
                    OsUtils.unjarOnWindows(installDir, dest.getAbsolutePath(), true);
                } else {
                    OsUtils.exec(new String[] {"tar", "xzf", dest.getAbsolutePath()}, new File(installDir), true);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Installation cancelled");
            } catch (Exception e) {
                throw new RuntimeException("Failed to unpack install archive: " + dest.getAbsolutePath(), e);
            }
        }

        mongod = new File(installDir + File.separator + fileName + File.separator + "bin"
                + File.separator + (osArch.isWindows() ? "mongod.exe" : "mongod"));

        // sanity check - mongod should exist
        if (!mongod.isFile()) {
            throw new RuntimeException("Installation seems to be corrupted - mongod is missing: " + mongod.getAbsolutePath());
        }
    }

    public void download(URL url, File dest) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        InputStream is = con.getInputStream();
        OutputStream os = new BufferedOutputStream(new FileOutputStream(dest));
        int len = con.getContentLength();

        byte [] buf = new byte[8192];
        int rc, total = 0;

        while ((rc = is.read(buf)) != -1) {
            os.write(buf, 0, rc);
            total += rc;
            consoleProgress(total, len);
        }
        // newline to escape from progress bar
        System.out.println();

        os.close();
        try {
            is.close();
        } catch (Exception ignored) {}

        // check length
        if (len != dest.length()) {
            throw new RuntimeException("Failed to fully download: " + url);
        }
    }

    public void fakeDownload(URL url, File dest) throws IOException {
        int total = 87000000;
        for (int i = 0; i < total; i += 1234567) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            consoleProgress(i, total);
        }
        consoleProgress(total, total);
        System.out.println();
    }

    private void consoleProgress(int progress, int max) {

        // format a new line
        // [====>                     ] 234kb/1.3Mb
        StringBuilder sb = new StringBuilder("[");
        int progressChars = (int) (progress / (double) max * 64);
        if (progressChars == lastProgress) {
            return;
        }
        lastProgress = progressChars;

        for (int i = 0; i < progressChars; i++) {
            sb.append("=");
        }
        sb.append(">");
        for (int i = progressChars; i < 64; i++) {
            sb.append(" ");
        }
        sb.append("] ");

        sb.append(formatBytes(progress));
        sb.append("/");
        sb.append(formatBytes(max));

        // clear previous line first
        System.out.print(CLEAR);
        System.out.print(sb);
    }

    private String formatBytes(int bytes) {
        if (bytes < 1000) {
            return String.format("%3db", bytes);
        } else if (bytes < 1000000) {
            return String.format("%3dkb", bytes / 1000);
        } else {
            return String.format("%3dMb", bytes / 1000000);
        }
    }

    public static void main(String [] args) throws IOException {
        try {
            new MongoInstaller().performInstall();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
