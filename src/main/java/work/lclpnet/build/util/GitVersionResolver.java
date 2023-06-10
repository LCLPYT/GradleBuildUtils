package work.lclpnet.build.util;

import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

public class GitVersionResolver {

    public static final String VERSION_PATTERN = "^[0-9]+\\.[0-9]+\\.[0-9]+(?:[-+][a-zA-Z0-9.]+)?$";
    private final Logger logger;

    public GitVersionResolver(Logger logger) {
        this.logger = logger;
    }

    /**
     * Fetches and verifies the latest git tag as version name.
     *
     * @param pwd The directory of the local git repository.
     * @throws IllegalStateException When the there is no git repository; not at least one tag; tag name mismatch; or another error.
     * @return The latest version fetched from git tags.
     */
    public String getGitVersion(File pwd) {
        try {
            // First, fetch all tags, to ensure all the tags are found
            ProcessExecutor.execProcess(logger, pwd, "git", "fetch", "--tags", "--force");

            // now actually get the latest tag with git describe
            ProcessResult result = ProcessExecutor.execProcess(logger, pwd, "git", "describe", "--tags", "--abbrev=0");

            // log outputs
            logger.info(result.stdout);

            if (!result.stderr.isEmpty()) {
                logger.error(result.stderr);
            }

            // throw on error
            if (result.exitCode != 0)
                throw new IOException("Git error, make sure you have at least one tag");

            return result.stdout.trim().split("\\r?\\n")[0];
        } catch (Exception ex) {
            throw new IllegalStateException("Could not determine version", ex);
        }
    }
}
