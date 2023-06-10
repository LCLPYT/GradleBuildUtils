package work.lclpnet.build.ext;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import work.lclpnet.build.util.GitVersionResolver;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;

public class BuildUtilsExtensionImpl implements BuildUtilsExtension {

    private final Project project;
    private final Property<String> versionPattern;
    private GitVersionResolver gitVersionResolver = null;

    public BuildUtilsExtensionImpl(Project project) {
        this.project = project;
        this.versionPattern = project.getObjects().property(String.class).convention(GitVersionResolver.VERSION_PATTERN);
    }

    @Override
    public String gitVersion() {
        final String version;

        Map<String, String> env = System.getenv();
        if (env.containsKey("CI_VERSION")) {
            version = env.get("CI_VERSION");
        } else {
            File pwd = project.getProjectDir();

            synchronized (this) {
                if (gitVersionResolver == null) {
                    gitVersionResolver = new GitVersionResolver(project.getLogger());
                }
            }

            version = gitVersionResolver.getGitVersion(pwd);
        }

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
}
