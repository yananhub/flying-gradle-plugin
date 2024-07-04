package tech.yanand.gradle;

/**
 * Whether or not an upload should be published automatically or manually after successful validation.
 */
public enum PublishingType {
    /**
     * After an upload is successfully validated, the artifact will be published automatically.
     * This is the default.
     */
    AUTOMATIC,

    /**
     * After an upload is successfully validated, the artifact will need to be
     * published manually.
     */
    USER_MANAGED;
}

