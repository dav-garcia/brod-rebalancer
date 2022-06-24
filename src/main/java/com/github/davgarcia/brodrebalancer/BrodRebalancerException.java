package com.github.davgarcia.brodrebalancer;

public class BrodRebalancerException extends RuntimeException {

    public BrodRebalancerException(final String message) {
        super(message);
    }

    public BrodRebalancerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BrodRebalancerException(final Throwable cause) {
        super(cause);
    }

    public BrodRebalancerException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
