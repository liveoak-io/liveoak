/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class OsUtils {

    public static void unjarOnWindows(String installDir, String archivePath, boolean log) throws IOException, InterruptedException {
        // locate jar tool
        String javaHome = System.getProperty("java.home");
        String jarExe = javaHome + "\\bin\\jar.exe";

        if (!new File(jarExe).isFile()) {
            jarExe = "jar.exe";
        }
        exec(new String[] {jarExe, "xf", archivePath}, new File(installDir), log, false);
    }

    public static String execWithResult(String[] cmdArgs, File curDir, boolean log) {
        return OsUtils.exec(cmdArgs, curDir, log, true);
    }

    public static String execWithOneLiner(String[] cmdArgs, File curDir, boolean log) {
        String result = OsUtils.exec(cmdArgs, curDir, log, true);

        // use only the first line of result:
        String [] lines = result.split("\\n");
        if (lines.length > 0) {
            result = lines[0].trim();
        } else {
            result = null;
        }
        return result;
    }

    public static void exec(String[] cmdArgs, File curDir, boolean log) {
        OsUtils.exec(cmdArgs, curDir, log, false);
    }

    private static String exec(String[] cmdArgs, File curDir, boolean log, boolean buffer) {
        Process proc;
        ProcessOutputLogger std, err;
        try {
            proc = Runtime.getRuntime().exec(cmdArgs, null, curDir);

            std = new ProcessOutputLogger("[std] ", proc.getInputStream(), log, buffer);
            err = new ProcessOutputLogger("[err] ", proc.getErrorStream(), log, buffer);

            if (proc.waitFor() == 0) {
                return std.complete().getBufferedOutput();
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to execute native command: " + Arrays.asList(cmdArgs), e);
        }

        if (err != null) {
            throw new RuntimeException("Execution ended with err status: " + proc.exitValue() + ":\n" + err.complete().getBufferedOutput());
        } else {
            throw new RuntimeException("Execution ended with err status: " + proc.exitValue());
        }
    }

    public static OsArch determineOSAndArch() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch");

        //System.out.println("OS: " + os + ", architecture: " + arch);
        if (arch.equals("amd64")) {
            arch = "x86_64";
        }

        if (os.startsWith("linux")) {
            if (arch.equals("x86") || arch.equals("i386") || arch.equals("i586")) {
                arch = "i686";
            }
            return new OsArch("linux", arch);
        } else if (os.startsWith("windows")) {
            if (arch.equals("x86")) {
                arch = "i386";
            }
            if (os.indexOf("2008") != -1 || os.indexOf("2003") != -1 || os.indexOf("vista") != -1) {
                return new OsArch("win32", arch, true);
            } else {
                return new OsArch("win32", arch);
            }
        } else if (os.startsWith("sunos")) {
            return new OsArch("sunos5", "x86_64");
        } else if (os.startsWith("mac os x")) {
            return new OsArch("osx", "x86_64");
        }

        // unsupported platform
        throw new RuntimeException("Could not determine available MongoDB version for this operating system: " + os);
    }

    private static class ProcessOutputLogger extends Thread {

        private InputStream is;
        private String logHead;
        private StringWriter buf = new StringWriter();

        private boolean log;
        private boolean buffer;

        ProcessOutputLogger(String logHead, InputStream is, boolean log, boolean buffer) {
            this.logHead = logHead;
            this.is = is;
            this.log = log;
            this.buffer = buffer;
            start();
        }

        public void run() {
            BufferedReader rf = new BufferedReader(new InputStreamReader(is));
            String line;
            try {
                while ((line = rf.readLine()) != null) {
                    if (log) {
                        System.out.println(logHead + line);
                    }
                    if (buffer) {
                        buf.write(line + System.lineSeparator());
                    }
                }
            } catch (IOException e) {
                // probably interrupted
                if (log) {
                    System.out.println(logHead + "- interrupted -");
                }
            }
        }

        private String getBufferedOutput() {
            return buf.toString();
        }

        private ProcessOutputLogger complete() {
            try {
                join();
            } catch (InterruptedException ignored) {}

            return this;
        }
    }
}
