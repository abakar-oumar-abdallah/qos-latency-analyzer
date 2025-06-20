package com.example.helloworldmvc.model;

public class HelloWorldModel {

    private String message;

    public HelloWorldModel() {
        this.message = "Hello World !";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}