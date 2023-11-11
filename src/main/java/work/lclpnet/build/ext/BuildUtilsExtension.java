package work.lclpnet.build.ext;

import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublishingExtension;

import java.util.Properties;

public interface BuildUtilsExtension {

    String latestTag();

    String gitVersion();

    Property<String> getVersionPattern();

    Property<String> getFallbackVersion();

    /**
     * Loads properties from a source, such as a file or filename.
     * The properties are loaded into this extension.
     * Any existing properties are replaced by the new values.
     * @param src The property source, can be a {@link java.io.File} or file name.
     * @return All properties that were loaded to this point-
     */
    Properties loadProperties(Object src);

    void setupPublishRepository(RepositoryHandler repositories, Properties properties);

    default void setupPublishRepository(PublishingExtension extension, Properties properties) {
        extension.repositories(repositories -> setupPublishRepository(repositories, properties));
    }

    default void setupPublishRepository(PublishingExtension extension) {
        setupPublishRepository(extension, new Properties());
    }
}
