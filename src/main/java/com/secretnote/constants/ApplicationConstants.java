package com.secretnote.constants;


public final class ApplicationConstants {

    public static final String JWT_SECRET_KEY = "JWT_SECRET";
    public static final String JWT_SECRET_DEFAULT_VALUE = "jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4";
    public static final String JWT_HEADER = "Authorization";

    private ApplicationConstants() {}

    public static String getJwtSecret() {
        String secret = System.getenv(JWT_SECRET_KEY);

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET environment variable is required"
            );
        }

        return secret;
    }


}
