package org.example;

public class InvalidCommandException extends RuntimeException{
    public InvalidCommandException(String string) {
        super(string);
    }
}
