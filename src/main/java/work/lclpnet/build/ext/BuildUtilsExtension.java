package work.lclpnet.build.ext;

import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublishingExtension;

import java.util.Properties;

public interface BuildUtilsExtension {

    String latestTag();

    String gitVersion();

    Property<String> getVersionPattern();

    Properties loadProperties(Object src);

    void setupPublishRepository(RepositoryHandler repositories, Properties properties);

    default void setupPublishRepository(PublishingExtension extension, Properties properties) {
        extension.repositories(repositories -> setupPublishRepository(repositories, properties));
    }

    default void setupPublishRepository(PublishingExtension extension) {
        setupPublishRepository(extension, new Properties());
    }
}
