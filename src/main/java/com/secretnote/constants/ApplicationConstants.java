package com.secretnote.constants;


public final class ApplicationConstants {

    public static final String JWT_SECRET_KEY = "JWT_SECRET";
    public static final String JWT_SECRET_DEFAULT_VALUE = "jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4";
    public static final String JWT_HEADER = "Authorization";

    private ApplicationConstants() {}

    public static String getJwtSecret() {
        return System.getenv(JWT_SECRET_KEY) != null
                ? System.getenv(JWT_SECRET_KEY)
                : JWT_SECRET_DEFAULT_VALUE;
    }



}
