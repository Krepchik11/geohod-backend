package me.geohod.geohodbackend.exception;

public class GeohodException extends RuntimeException {
    public GeohodException(String message) {
        super(message);
    }

    public GeohodException(String message, Throwable cause) {
        super(message, cause);
    }
}
