package me.qscbm.plugins.dodochat.common;

public class DodoCommandSourceException extends RuntimeException {
    public DodoCommandSourceException(String message) {
        super(message);
    }

    public DodoCommandSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DodoCommandSourceException(Throwable cause) {
        super(cause);
    }

    protected DodoCommandSourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
