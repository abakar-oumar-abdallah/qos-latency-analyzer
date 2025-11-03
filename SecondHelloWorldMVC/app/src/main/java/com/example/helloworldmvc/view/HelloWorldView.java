package com.example.helloworldmvc.view;

import android.widget.TextView;
public class HelloWorldView {
    protected TextView textView;
    public HelloWorldView(TextView textView) {
        this.textView = textView;
    }
    public void displayMessage(String message) {
        textView.setText(message);
    }
    public void showError(String error) {
        textView.setText("Erreur: " + error);
    }
}