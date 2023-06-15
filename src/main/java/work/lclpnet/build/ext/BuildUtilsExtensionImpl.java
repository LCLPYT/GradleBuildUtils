package work.lclpnet.build.ext;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublishingExtension;
import work.lclpnet.build.util.GitVersionResolver;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class BuildUtilsExtensionImpl implements BuildUtilsExtension {

    private final Project project;
    private final Property<String> versionPattern;
    private GitVersionResolver gitVersionResolver = null;
    private String latestTag = null;

    public BuildUtilsExtensionImpl(Project project) {
        this.project = project;
        this.versionPattern = project.getObjects().property(String.class).convention(GitVersionResolver.VERSION_PATTERN);
    }

    @Override
    public String latestTag() {
        Map<String, String> env = System.getenv();

        if (env.containsKey("CI_VERSION")) {
            return env.get("CI_VERSION");
        }

        if (env.containsKey("GITHUB_REF")) {
            String githubRef = env.get("GITHUB_REF");

            String prefix = "refs/tags/";

            if (githubRef.startsWith(prefix)) {
                return githubRef.substring(prefix.length());
            }
        }

        if (latestTag != null) return latestTag;

        return fetchLatestTag();
    }

    @Override
    public String gitVersion() {
        final String version = latestTag();

        String versionPattern = getVersionPattern().get();
        if (!version.matches(versionPattern)) {
            throw new IllegalStateException(String.format("Latest tag '%s' does not match the required versioning scheme", version));
        }

        if ("true".equals(System.getProperty("build.local"))) {
            return version + "+local";
        }

        project.getLogger().lifecycle("Project version: {}", version);

        return version;
    }

    @Override
    public Properties loadProperties(Object src) {
        final File file;

        if (src instanceof File) {
            file = (File) src;
        } else if (src instanceof String) {
            file = new File(project.getProjectDir(), (String) src);
        } else {
            throw new IllegalArgumentException(String.format("Unsupported argument type %s; expected %s or %s",
                    src.getClass().getName(),
                    File.class.getName(),
                    String.class.getName()));
        }

        final Properties props = new Properties();

        if (file.exists()) {
            try (InputStream in = Files.newInputStream(file.toPath())) {
                props.load(in);
            } catch (Exception ex) {
                throw new IllegalStateException(String.format("Error loading %s", file.getAbsolutePath()), ex);
            }
        }

        return props;
    }

    @Override
    public Property<String> getVersionPattern() {
        return versionPattern;
    }

    private synchronized String fetchLatestTag() {
        if (latestTag != null) return latestTag;

        File pwd = project.getProjectDir();

        synchronized (this) {
            if (gitVersionResolver == null) {
                gitVersionResolver = new GitVersionResolver(project.getLogger());
            }
        }

        return gitVersionResolver.getGitVersion(pwd);
    }

    @Override
    public void setupPublishRepository(PublishingExtension extension, Properties props) {
        extension.repositories(repositories -> repositories.maven(repo -> {
            Map<String, String> env = System.getenv();

            if (Stream.of("DEPLOY_URL", "DEPLOY_USER", "DEPLOY_PASSWORD").allMatch(env::containsKey)) {
                repo.credentials(credentials -> {
                    credentials.setUsername(env.get("DEPLOY_USER"));
                    credentials.setPassword(env.get("DEPLOY_PASSWORD"));
                });

                repo.setUrl(env.getProperty("DEPLOY_URL"));
            } else if (Stream.of("mavenHost", "mavenUser", "mavenPassword").allMatch(props::containsKey)) {
                repo.credentials(credentials -> {
                    credentials.setUsername(props.getProperty("mavenUser"));
                    credentials.setPassword(props.getProperty("mavenPassword"));
                });

                repo.setUrl(props.getProperty("mavenHost"));
            } else {
                repo.setUrl("file:///" + project.getProjectDir().getAbsolutePath() + "/repo");
            }
        }));
    }
}
