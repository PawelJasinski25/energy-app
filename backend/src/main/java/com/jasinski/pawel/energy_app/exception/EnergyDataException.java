package com.jasinski.pawel.energy_app.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class EnergyDataException extends RuntimeException {
    public EnergyDataException(String message) {
        super(message);
    }

    public EnergyDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
