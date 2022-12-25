package com.sg.authentication.exception;

import javax.persistence.NoResultException;

public class DuplicateMemberException extends NoResultException {
    public DuplicateMemberException(String message) {
        super(message);
    }
}
