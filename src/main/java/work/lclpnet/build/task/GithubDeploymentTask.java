package work.lclpnet.build.task;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Objects;

public abstract class GithubDeploymentTask extends DefaultTask {

    @InputFiles
    public abstract SetProperty<RegularFile> getAssets();

    private final Config config = new Config();
    private final ReleaseConfig releaseConfig = new ReleaseConfig();

    public GithubDeploymentTask() {
        this.onlyIf(task -> config.token != null && config.repository != null);
        this.setGroup(PublishingPlugin.PUBLISH_TASK_GROUP);
    }

    @TaskAction
    public void deploy() throws IOException {
        final GitHub github = GitHub.connectUsingOAuth(config.token);
        final GHRepository repository = github.getRepository(config.repository);

        final Project project = getProject();
        final String version = project.getVersion().toString();

        final GHReleaseBuilder releaseBuilder = new GHReleaseBuilder(repository, version);

        releaseBuilder.name(Objects.requireNonNull(releaseConfig.title, "Release title is null"));

        if (releaseConfig.description != null) {
            releaseBuilder.body(releaseConfig.description);
        }

        if (releaseConfig.commitish != null) {
            releaseBuilder.commitish(releaseConfig.commitish);
        }

        releaseBuilder.prerelease(releaseConfig.preRelease);

        final GHRelease release = releaseBuilder.create();

        FileNameMap fileNameMap = URLConnection.getFileNameMap();

        for (RegularFile regularFile : getAssets().get()) {
            File file = regularFile.getAsFile();

            String contentType = fileNameMap.getContentTypeFor(file.getName());

            if (contentType == null) {
                contentType = "application/octet-stream";  // fallback content-type
            }

            release.uploadAsset(file, contentType);
        }
    }

    public void release(Action<ReleaseConfig> action) {
        action.execute(releaseConfig);
    }

    public void config(Action<Config> action) {
        action.execute(config);
    }

    public static class ReleaseConfig {
        public String tag = null;
        public String title = null;
        public String description = null;
        private String commitish = null;
        public boolean preRelease = false;

        /**
         * Configures to create a new tag on the specified branch with the creation of the release.
         * @param name The name of the tag.
         * @param branch The branch to create the tag on. If null, the repositories default branch is used.
         */
        public void createNewTag(String name, String branch) {
            tag = name;
            commitish = branch;
        }

        private ReleaseConfig() {}
    }

    public static class Config {

        public String token = null;
        public String repository = null;

        private Config() {}
    }
}
