package com.example.helloworldmvc.controller;

import com.example.helloworldmvc.model.HelloWorldModel;
import com.example.helloworldmvc.view.HelloWorldView;

public class HelloWorldController {

    private HelloWorldModel model;
    private HelloWorldView view;

    public HelloWorldController(HelloWorldModel model, HelloWorldView view) {
        this.model = model;
        this.view = view;
    }

    public void showMessage() {
        String message = model.getMessage();
        view.displayMessage(message);
    }

    public void updateMessage(String newMessage) {
        model.setMessage(newMessage);
        view.displayMessage(model.getMessage());
    }
}