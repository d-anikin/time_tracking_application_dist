package ru.dealerpoint.redmine;

public class ApiAuthorizationException extends ApiException {
    public ApiAuthorizationException(String errorMsg) {
        super(errorMsg);
    }
}
