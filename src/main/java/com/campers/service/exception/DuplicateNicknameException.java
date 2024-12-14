// src/main/java/com/campers/service/exception/DuplicateNicknameException.java
package com.campers.service.exception;

public class DuplicateNicknameException extends RuntimeException {
    public DuplicateNicknameException(String message) {
        super(message);
    }
}
