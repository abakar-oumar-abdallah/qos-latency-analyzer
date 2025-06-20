package com.example.helloworldmvc;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import com.example.helloworldmvc.model.HelloWorldModel;
import com.example.helloworldmvc.view.HelloWorldView;
import com.example.helloworldmvc.controller.HelloWorldController;

public class MainActivity extends AppCompatActivity {

    private HelloWorldController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main();
    }

    public void main() {
        TextView textView = findViewById(R.id.messageTextView);
        EditText editText = findViewById(R.id.messageEditText);
        Button showButton = findViewById(R.id.showButton);
        Button updateButton = findViewById(R.id.updateButton);

        HelloWorldModel model = new HelloWorldModel();
        HelloWorldView view = new HelloWorldView(textView);
        controller = new HelloWorldController(model, view);

        showButton.setOnClickListener(v -> controller.showMessage());
        updateButton.setOnClickListener(v -> {
            String newMessage = editText.getText().toString();
            controller.updateMessage(newMessage);
        });

        controller.showMessage();
    }
}