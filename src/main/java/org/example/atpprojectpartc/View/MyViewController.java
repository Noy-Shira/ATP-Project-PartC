package org.example.atpprojectpartc.View;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MyViewController implements IView{
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}