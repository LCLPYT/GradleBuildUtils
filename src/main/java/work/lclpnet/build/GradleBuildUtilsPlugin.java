package work.lclpnet.build;

import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.ExtraPropertiesExtension;

import java.io.File;
import java.util.Properties;

public class GradleBuildUtilsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        final File pwd = project.getProjectDir();

        final ExtensionContainer extensions = project.getExtensions();
        final ExtraPropertiesExtension extraProperties = extensions.getExtraProperties();

        final String pattern;
        if (extraProperties.has("versionPattern")) {
            pattern = (String) extraProperties.get("versionPattern");
        } else {
            pattern = null;
        }

        extraProperties.set("gitVersion", new Closure<String>(this, this) {
            @Override
            public String call() {
                if (pattern != null) return BuildUtils.getVersion(pwd, pattern);
                else return BuildUtils.getVersion(pwd);
            }
        });

        extraProperties.set("loadProperties", new Closure<Properties>(this, this) {
            @Override
            public Properties call(Object arg) {
                File file;
                if (arg instanceof File) file = (File) arg;
                else if (arg instanceof String) file = new File(pwd, (String) arg);
                else throw new IllegalArgumentException(String.format("Illegal argument type %s, expected %s or %s",
                            arg.getClass().getName(),
                            File.class.getName(),
                            String.class.getName()));

                return BuildUtils.loadProperties(file);
            }
        });
    }


}
