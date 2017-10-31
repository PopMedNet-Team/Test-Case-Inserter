/*
 * Decompiled with CFR 0_123.
 */
package com.commoninf;

public class TCIException
extends RuntimeException {
    static final long serialVersionUID = 1;

    public TCIException() {
    }

    public TCIException(String message) {
        super(message);
    }

    public TCIException(Throwable cause) {
        super(cause);
    }

    public TCIException(String message, Throwable cause) {
        super(message, cause);
    }

    public TCIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

