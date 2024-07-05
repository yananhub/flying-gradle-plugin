package tech.yanand.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

/**
 * The configuration of how to upload a bundles to maven central portal.
 *
 * @author Richard Zhang
 */
public interface MavenCentralExtension {

    /**
     * The extension name.
     */
    String NAME = "mavenCentral";

    /**
     * Upload URL for uploading a deployment bundle.
     *
     * @return URL string
     */
    Property<String> getUploadUrl();

    /**
     * Whether to publish automatically or manually after a successful upload.
     *
     * @return Publishing type
     */
    Property<String> getPublishingType();

    /**
     * The URL for retrieving status of a deployment.
     *
     * @return URL string
     */
    Property<String> getStatusUrl();

    /**
     * The authorization toke for calling central portal APIs
     *
     * @return Token string
     */
    Property<String> getAuthToken();

    /**
     * The repository directory for zipping the bundle.
     * It is usually a local directory published by the Maven publish plugin.
     *
     * @return Repository directory
     */
    DirectoryProperty getRepoDir();

    /**
     * Max wait time for status API to get 'PUBLISHING' or 'PUBLISHED' status when the
     * publishing type is 'AUTOMATIC', or additionally 'VALIDATED' when the publishing type is
     * 'USER_MANAGED'.
     *
     * @return Duration in seconds
     */
    Property<Integer> getMaxWait();
}