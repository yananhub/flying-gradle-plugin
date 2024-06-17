package tech.yanand.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Zip;

/**
 * Install a "mavenCentral" extension and register a "publishToMavenCentralPortal" task.
 * Also register a "zipBundleForUpload" to compress the bundle.
 *
 * @author Richard Zhang
 */
public abstract class MavenCentralPublishPlugin implements Plugin<Project> {

    private static final String CENTRAL_PUBLISH_TASK_GROUP = "central publish";

    @Override
    public void apply(Project project) {
        MavenCentralExtension extension = project.getExtensions()
                .create(MavenCentralExtension.class, MavenCentralExtension.NAME, DefaultMavenCentralExtension.class, project.getObjects());
        TaskContainer taskContainer = project.getTasks();

        Zip zipTask = taskContainer.register("zipBundleForUpload", Zip.class).get();
        zipTask.setGroup(CENTRAL_PUBLISH_TASK_GROUP);
        zipTask.from(extension.getRepoDir());

        UploadToCentralPortalTask uploadTask = taskContainer
                .register(UploadToCentralPortalTask.NAME, UploadToCentralPortalTask.class).get();
        uploadTask.setGroup(CENTRAL_PUBLISH_TASK_GROUP);
        uploadTask.dependsOn(zipTask);

        uploadTask.getUploadFile().value(zipTask.getArchiveFile());
    }
}