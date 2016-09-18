package ru.dealerpoint.redmine;

public class ApiWrongKeyException extends ApiException {
    public ApiWrongKeyException(String errorMsg) {
        super(errorMsg);
    }
}
