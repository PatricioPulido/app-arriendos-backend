package com.apparriendos.arriendos_api.storage;

public class StorageException extends RuntimeException {
    public StorageException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
