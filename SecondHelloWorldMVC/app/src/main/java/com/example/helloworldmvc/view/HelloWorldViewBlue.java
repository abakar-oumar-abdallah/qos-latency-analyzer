package com.example.helloworldmvc.view;

import android.graphics.Color;
import android.widget.TextView;
public class HelloWorldViewBlue extends HelloWorldView {
    public HelloWorldViewBlue(TextView textView) {
        super(textView); // Appelle le constructeur de HelloWorldView
        applyBlueStyle(); // Applique le style bleu spécifique
    }
    private void applyBlueStyle() {
        // Couleur de fond bleu clair
        textView.setBackgroundColor(Color.parseColor("#E3F2FD"));

        // Couleur du texte bleu foncé
        textView.setTextColor(Color.parseColor("#0D47A1"));

        // Taille du texte plus grande
        textView.setTextSize(20);

        // Centrage du texte
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);

        // Padding pour l'espace autour du texte
        textView.setPadding(32, 32, 32, 32);
    }

    @Override
    public void displayMessage(String message) {
        super.displayMessage("🔵 VUE BLEUE: " + message);
    }
    @Override
    public void showError(String error) {
        textView.setText("🔵 ERREUR BLEUE: " + error);
        textView.setTextColor(Color.parseColor("#B71C1C")); // Rouge pour erreur
    }
}