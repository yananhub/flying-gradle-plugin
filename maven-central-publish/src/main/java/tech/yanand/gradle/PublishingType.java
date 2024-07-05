package tech.yanand.gradle;

/**
 * Whether or not an upload should be published automatically or manually after successful validation.
 */
final class PublishingType {
    private PublishingType() {
        // Instantiation is not allowed
    }

    /**
     * After an upload is successfully validated, the artifact will be published automatically.
     * This is the default.
     */
    static final String AUTOMATIC = "AUTOMATIC";

    /**
     * After an upload is successfully validated, the artifact will need to be
     * published manually.
     */
    static final String USER_MANAGED = "USER_MANAGED";
}