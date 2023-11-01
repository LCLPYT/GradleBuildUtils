package work.lclpnet.build.ext;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.provider.Property;
import work.lclpnet.build.util.GitVersionResolver;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static work.lclpnet.build.Constants.*;

public class BuildUtilsExtensionImpl implements BuildUtilsExtension {

    private final Project project;
    private final Property<String> versionPattern;
    private final Properties properties = new Properties();
    private GitVersionResolver gitVersionResolver = null;
    private volatile String latestTag = null;

    public BuildUtilsExtensionImpl(Project project) {
        this.project = project;
        this.versionPattern = project.getObjects().property(String.class).convention(GitVersionResolver.VERSION_PATTERN);
    }

    @Override
    public String latestTag() {
        Map<String, String> env = System.getenv();

        if (env.containsKey(ENV_CI_VERSION)) {
            return env.get(ENV_CI_VERSION);
        }

        if (env.containsKey(ENV_GITHUB_REF)) {
            String githubRef = env.get(ENV_GITHUB_REF);

            String prefix = "refs/tags/";

            if (githubRef.startsWith(prefix)) {
                return githubRef.substring(prefix.length());
            }
        }

        return fetchLatestTag();
    }

    @Override
    public String gitVersion() {
        String version = latestTag();

        String versionPattern = getVersionPattern().get();
        if (!version.matches(versionPattern)) {
            throw new IllegalStateException(String.format("Latest tag '%s' does not match the required versioning scheme", version));
        }

        if ("true".equals(System.getProperty(PROP_BUILD_LOCAL))) {
            version += "+local";
        }

        if (properties.containsKey(PROP_VERSION_OVERRIDE)) {
            version = properties.getProperty(PROP_VERSION_OVERRIDE);
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

        if (file.exists()) {
            try (InputStream in = Files.newInputStream(file.toPath())) {
                properties.load(in);
            } catch (Exception ex) {
                throw new IllegalStateException(String.format("Error loading %s", file.getAbsolutePath()), ex);
            }
        }

        return properties;
    }

    @Override
    public Property<String> getVersionPattern() {
        return versionPattern;
    }

    private String fetchLatestTag() {
        if (latestTag != null) {
            return latestTag;
        }

        synchronized (this) {
            if (latestTag != null) {
                return latestTag;
            }

            if (gitVersionResolver == null) {
                gitVersionResolver = new GitVersionResolver(project.getLogger());
            }

            File pwd = project.getProjectDir();

            latestTag = gitVersionResolver.getGitVersion(pwd);

            return latestTag;
        }
    }

    @Override
    public void setupPublishRepository(RepositoryHandler repositories, Properties props) {
        repositories.maven(repo -> {
            Map<String, String> env = System.getenv();

            if (Stream.of(ENV_DEPLOY_URL, ENV_DEPLOY_USER, ENV_DEPLOY_PASSWORD).allMatch(env::containsKey)) {
                repo.credentials(credentials -> {
                    credentials.setUsername(env.get(ENV_DEPLOY_USER));
                    credentials.setPassword(env.get(ENV_DEPLOY_PASSWORD));
                });

                repo.setUrl(env.get(ENV_DEPLOY_URL));
                return;
            }

            if (project.getVersion().toString().endsWith("-SNAPSHOT") && Stream.of(PROP_SNAPSHOT_URL, PROP_MAVEN_USER,
                    PROP_MAVEN_PASSWORD).allMatch(props::containsKey)) {

                repo.credentials(credentials -> {
                    credentials.setUsername(props.getProperty(PROP_MAVEN_USER));
                    credentials.setPassword(props.getProperty(PROP_MAVEN_PASSWORD));
                });

                repo.setUrl(props.getProperty(PROP_SNAPSHOT_URL));
                return;
            }

            if (Stream.of(PROP_MAVEN_URL, PROP_MAVEN_USER, PROP_MAVEN_PASSWORD).allMatch(props::containsKey)) {
                repo.credentials(credentials -> {
                    credentials.setUsername(props.getProperty(PROP_MAVEN_USER));
                    credentials.setPassword(props.getProperty(PROP_MAVEN_PASSWORD));
                });

                repo.setUrl(props.getProperty(PROP_MAVEN_URL));
                return;
            }

            repo.setUrl("file:///" + project.getProjectDir().getAbsolutePath() + "/repo");
        });
    }
}
