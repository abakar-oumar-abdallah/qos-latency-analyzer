package com.example.helloworldmvc.view;

import android.graphics.Color;
import android.widget.TextView;

public class HelloWorldViewRed extends HelloWorldView {
    public HelloWorldViewRed(TextView textView) {
        super(textView); // Appelle le constructeur de HelloWorldView
        applyRedStyle(); // Applique le style rouge spécifique
    }

    private void applyRedStyle() {
        // Couleur de fond rouge clair
        textView.setBackgroundColor(Color.parseColor("#FFEBEE"));

        // Couleur du texte rouge foncé
        textView.setTextColor(Color.parseColor("#B71C1C"));

        // Taille du texte plus grande
        textView.setTextSize(22);

        // Centrage du texte
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);

        // Padding pour l'espace autour du texte
        textView.setPadding(40, 40, 40, 40);
    }
    @Override
    public void displayMessage(String message) {
        super.displayMessage("🔴 VUE ROUGE: " + message);
    }

    @Override
    public void showError(String error) {
        textView.setText("🔴 ERREUR ROUGE: " + error);
        textView.setTextColor(Color.parseColor("#000000")); // Noir pour erreur
    }
}