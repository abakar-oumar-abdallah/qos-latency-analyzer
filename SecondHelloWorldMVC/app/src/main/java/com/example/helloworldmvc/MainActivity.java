package com.example.helloworldmvc;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import com.example.helloworldmvc.model.HelloWorldModel;
import com.example.helloworldmvc.view.HelloWorldView;
import com.example.helloworldmvc.view.HelloWorldViewBlue;
import com.example.helloworldmvc.view.HelloWorldViewRed;
import com.example.helloworldmvc.controller.HelloWorldController;

public class MainActivity extends AppCompatActivity {


    private HelloWorldController controller;
    private HelloWorldModel model;

    private HelloWorldViewBlue blueView;    // Vue avec style bleu
    private HelloWorldViewRed redView;      // Vue avec style rouge
    private HelloWorldView currentView;     // Vue actuellement active


    private TextView messageTextView;
    private EditText messageEditText;
    private Button showButton;
    private Button updateButton;
    private Button changeViewButton;        // NOUVEAU : changer de vue
    private Button evolveModelButton;       // NOUVEAU : faire évoluer modèle
    private TextView viewIndicator;         // NOUVEAU : indicateur de vue active

    private boolean isBlueViewActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main();
    }

    public void main() {

        initializeViews();

        model = new HelloWorldModel();

        createMultipleViews();

        controller = new HelloWorldController(model, currentView);

        setupEventListeners();

        controller.showMessage();
        updateViewIndicator();
    }

    private void initializeViews() {
        messageTextView = findViewById(R.id.messageTextView);
        messageEditText = findViewById(R.id.messageEditText);
        showButton = findViewById(R.id.showButton);
        updateButton = findViewById(R.id.updateButton);
        changeViewButton = findViewById(R.id.changeViewButton);      // NOUVEAU
        evolveModelButton = findViewById(R.id.evolveModelButton);    // NOUVEAU
        viewIndicator = findViewById(R.id.viewIndicator);            // NOUVEAU
    }

    /**
     * NOUVEAU : CRÉATION DES MULTIPLES VUES
     * Action : crée les instances des vues bleue et rouge
     * Résultat : plusieurs vues disponibles pour le même TextView
     */
    private void createMultipleViews() {
        // Crée la vue bleue avec le même TextView
        blueView = new HelloWorldViewBlue(messageTextView);

        // Crée la vue rouge avec le même TextView
        redView = new HelloWorldViewRed(messageTextView);

        // Démarre avec la vue bleue active
        currentView = blueView;
        isBlueViewActive = true;
    }

    private void setupEventListeners() {

        showButton.setOnClickListener(v -> {
            controller.showMessage();
        });

        updateButton.setOnClickListener(v -> {
            String newMessage = messageEditText.getText().toString();
            controller.updateMessage(newMessage);
            messageEditText.setText("");
        });


        changeViewButton.setOnClickListener(v -> {

            if (isBlueViewActive) {
                currentView = redView;           // Passe à la vue rouge
                isBlueViewActive = false;
            } else {
                currentView = blueView;          // Passe à la vue bleue
                isBlueViewActive = true;
            }

            controller.changeView(currentView);

            updateViewIndicator();
        });

        evolveModelButton.setOnClickListener(v -> {
            controller.evolveModel(); // Le contrôleur gère tout
        });
    }
    private void updateViewIndicator() {
        if (isBlueViewActive) {
            viewIndicator.setText("🔵 Vue active : BLEUE");
        } else {
            viewIndicator.setText("🔴 Vue active : ROUGE");
        }
    }
}