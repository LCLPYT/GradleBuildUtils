package work.lclpnet.build;

import java.io.*;
import java.util.Properties;
import java.util.stream.Collectors;

public class BuildUtils {

    public static final String VERSION_PATTERN = "^[0-9]+\\.[0-9]+\\.[0-9]+(?:[-+][a-zA-Z0-9.]+)?$";

    /**
     * Fetches and verifies the latest git tag as version name.
     *
     * @param pwd The directory of the local git repository.
     * @throws IllegalStateException When the there is no git repository; not at least one tag; tag name mismatch; or another error.
     * @return The latest version fetched from git tags.
     */
    public static String getVersion(File pwd) {
        return getVersion(pwd, VERSION_PATTERN);
    }

    /**
     * Fetches and verifies the latest git tag as version name.
     *
     * @param pwd The directory of the local git repository.
     * @param regex The regex pattern which must match the version.
     * @throws IllegalStateException When the there is no git repository; not at least one tag; tag name mismatch; or another error.
     * @return The latest version fetched from git tags.
     */
    public static String getVersion(File pwd, String regex) {
        try {
            String version;
            if (System.getenv().containsKey("CI_VERSION")) version = System.getenv().get("CI_VERSION");
            else {
                // First, fetch all tags, to ensure all the tags are found
                execProcess(pwd, "git", "fetch", "--tags", "--force");

                // now actually get the latest tag with git describe
                ProcessResult result = execProcess(pwd, "git", "describe", "--tags", "--abbrev=0");

                // log outputs
                System.out.println(result.stdout);
                if (!result.stderr.isEmpty()) System.err.println(result.stderr);

                // throw on error
                if (result.exitCode != 0)
                    throw new IOException("Git error, make sure you have at least one tag");

                version = result.stdout.trim().split("\\r?\\n")[0];
            }

            if (!version.matches(regex))
                throw new IllegalStateException(String.format("Latest tag '%s' does not match the required versioning scheme", version));

            return version;
        } catch (Exception ex) {
            IllegalStateException wrapper = new IllegalStateException("Could not determine version");
            wrapper.addSuppressed(ex);
            throw wrapper;
        }
    }

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
        ProcessBuilder builder = new ProcessBuilder(args)
                .directory(pwd);

        System.out.printf("Executing '%s' in '%s' ...\n",
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
        System.out.printf("Process exited with code %s\n", exitCode);
        return new ProcessResult(exitCode, stdout, stderr);
    }

    /**
     * Loads a java properties file.
     * If the file does not exist, or if there is any other error, the returned properties are empty.
     *
     * @param file The properties file.
     * @return The loaded properties (empty, if there was an error).
     */
    public static Properties loadProperties(File file) {
        final Properties props = new Properties();

        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (Exception ex) {
                IllegalStateException wrapper = new IllegalStateException(String.format("Error loading %s", file.getAbsolutePath()));
                wrapper.addSuppressed(ex);
                throw wrapper;
            }
        }

        return props;
    }
}
