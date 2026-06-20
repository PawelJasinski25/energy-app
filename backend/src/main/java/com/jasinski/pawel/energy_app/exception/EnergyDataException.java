package com.jasinski.pawel.energy_app.exception;


public class EnergyDataException extends RuntimeException {
    public EnergyDataException(String message) {
        super(message);
    }

    public EnergyDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
