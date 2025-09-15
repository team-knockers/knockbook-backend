package com.knockbook.backend.exception;

public class InvalidRequestParameterException extends ApplicationException {

    public InvalidRequestParameterException(String parameterName, String parameterValue) {
        super("INVALID_REQUEST_PARAMETER",
                "Invalid request parameter: %s=%s".formatted(parameterName, parameterValue));
    }
}
