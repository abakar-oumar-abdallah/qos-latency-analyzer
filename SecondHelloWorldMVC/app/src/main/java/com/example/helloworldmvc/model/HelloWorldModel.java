package com.example.helloworldmvc.model;

public class HelloWorldModel {

    private String message;
    private int evolutionCounter;

    private String[] evolutionMessages = {
            "Hello World !",
            "Bonjour le Monde !",
            "Hola Mundo !",
    };

    public HelloWorldModel() {
        this.message = " Hello World !";
        this.evolutionCounter = 0; // Commence à l'index 0
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void evolveModel() {
        // Passe au message suivant dans le tableau
        evolutionCounter = (evolutionCounter + 1) % evolutionMessages.length;

        // Met à jour le message avec le nouveau de la liste
        this.message = evolutionMessages[evolutionCounter];
    }

    public String getModelInfo() {
        return "Message " + (evolutionCounter + 1) + "/" + evolutionMessages.length;
    }
}