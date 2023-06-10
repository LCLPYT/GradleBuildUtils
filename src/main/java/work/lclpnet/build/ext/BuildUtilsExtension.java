package work.lclpnet.build.ext;

import org.gradle.api.provider.Property;

import java.util.Properties;

public interface BuildUtilsExtension {

    String gitVersion();

    Property<String> getVersionPattern();

    Properties loadProperties(Object src);
}
