package work.lclpnet.build;

import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.util.Properties;

public class GradleBuildUtilsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        final File pwd = project.getProjectDir();

        project.getExtensions().getExtraProperties().set("gitVersion", new Closure<String>(this, this) {
            @Override
            public String call() {
                return BuildUtils.getVersion(pwd);
            }
        });

        project.getExtensions().getExtraProperties().set("loadProperties", new Closure<Properties>(this, this) {
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
