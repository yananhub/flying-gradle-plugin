/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.yanand.gradle.mavenpublish;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.tasks.Delete;
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
        Provider<Directory> bundleRepoDir = project.getLayout().getBuildDirectory().dir("repos/bundles");

        PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);
        publishingExtension.getRepositories().maven(repo -> {
            repo.setName("mavenCentralLocal");
            repo.setUrl(bundleRepoDir);
        });

        TaskContainer taskContainer = project.getTasks();

        Delete deleteTask = taskContainer.register("deleteBundleRepo", Delete.class).get();
        deleteTask.setGroup(CENTRAL_PUBLISH_TASK_GROUP);
        deleteTask.delete(bundleRepoDir);

        Task publishTask = taskContainer.getByName("publish");
        publishTask.dependsOn(deleteTask);

        Zip zipTask = taskContainer.register("zipBundleForUpload", Zip.class).get();
        zipTask.setGroup(CENTRAL_PUBLISH_TASK_GROUP);
        zipTask.from(bundleRepoDir);
        zipTask.dependsOn(publishTask);

        PublishToCentralPortalTask uploadTask = taskContainer
                .register(PublishToCentralPortalTask.NAME, PublishToCentralPortalTask.class).get();
        uploadTask.setGroup(CENTRAL_PUBLISH_TASK_GROUP);
        uploadTask.dependsOn(zipTask);

        uploadTask.getUploadFile().value(zipTask.getArchiveFile());
    }
}