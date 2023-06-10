package work.lclpnet.build;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import work.lclpnet.build.ext.BuildUtilsExtension;
import work.lclpnet.build.ext.BuildUtilsExtensionImpl;

public class GradleBuildUtilsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        final ExtensionContainer extensions = project.getExtensions();
        extensions.create(BuildUtilsExtension.class, "buildUtils", BuildUtilsExtensionImpl.class, project);
    }
}
