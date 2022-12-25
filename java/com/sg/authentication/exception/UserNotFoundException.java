package com.sg.authentication.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String userNmae){
        super(userNmae + " NotFoundException");
    }
}
