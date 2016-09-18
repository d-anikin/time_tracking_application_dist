package ru.dealerpoint.redmine;

import java.io.IOException;

public class ApiException extends IOException {
    public ApiException(String errorMsg) {
        super(errorMsg);
    }
}