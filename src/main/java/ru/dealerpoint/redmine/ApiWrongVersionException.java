package ru.dealerpoint.redmine;

public class ApiWrongVersionException extends ApiException {
    public ApiWrongVersionException(String errorMsg) {
        super(errorMsg);
    }
}
