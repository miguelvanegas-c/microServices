package eci.edu.co.monolito.exception;

public class UserExceptionNotFound extends RuntimeException{
    public UserExceptionNotFound (String message){
        super(message);
    }
}
