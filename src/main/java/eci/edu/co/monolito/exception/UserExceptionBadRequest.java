package eci.edu.co.monolito.exception;

public class UserExceptionBadRequest extends RuntimeException{
    public UserExceptionBadRequest (String message){
        super(message);
    }
}
