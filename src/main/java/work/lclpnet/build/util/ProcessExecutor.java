package work.lclpnet.build.util;

import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.io.*;
import java.util.stream.Collectors;

public class ProcessExecutor {

    private ProcessExecutor() {}

    /**
     * Executes a process with args inside the given working directory.
     *
     * @param pwd The working directory.
     * @param args The program name and arguments, as if someone would type in the CLI.
     * @return The exitCode, stdout and stderr of the process.
     * @throws IOException When there was an I/O error of any kind.
     * @throws InterruptedException When the thread was interrupted while waiting for the process to terminate.
     */
    public static ProcessResult execProcess(File pwd, String... args) throws IOException, InterruptedException {
        return execProcess(NOPLogger.NOP_LOGGER, pwd, args);
    }

    /**
     * Executes a process with args inside the given working directory.
     *
     * @param logger A logger for information.
     * @param pwd The working directory.
     * @param args The program name and arguments, as if someone would type in the CLI.
     * @return The exitCode, stdout and stderr of the process.
     * @throws IOException When there was an I/O error of any kind.
     * @throws InterruptedException When the thread was interrupted while waiting for the process to terminate.
     */
    public static ProcessResult execProcess(Logger logger, File pwd, String... args) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(args)
                .directory(pwd);

        logger.info("Executing '{}' in '{}' ...",
                String.join(" ", builder.command()),
                builder.directory().getAbsolutePath());

        Process process = builder.start();
        String stdout, stderr;
        try (InputStream outIn = process.getInputStream();
             InputStream errIn = process.getErrorStream()) {

            stdout = new BufferedReader(new InputStreamReader(outIn))
                    .lines().collect(Collectors.joining("\n"));

            stderr = new BufferedReader(new InputStreamReader(errIn))
                    .lines().collect(Collectors.joining("\n"));
        }

        int exitCode = process.waitFor();

        logger.info("Process exited with code {}", exitCode);

        return new ProcessResult(exitCode, stdout, stderr);
    }
}
