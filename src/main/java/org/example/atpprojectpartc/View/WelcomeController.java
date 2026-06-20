package org.example.atpprojectpartc.View;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.atpprojectpartc.ViewModel.MyViewModel;
import java.io.File;

public class WelcomeController {

    @FXML private Button startButton;
    @FXML private Button loadButton;

    private MyViewModel viewModel;

    // Inject the ViewModel from HelloApplication
    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML
    private void onStart() {
        // Switch to the game scene directly
        switchToGameScene();
    }

    @FXML
    private void onLoad() {
        // Open file chooser to select a saved maze
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Maze");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Maze Files", "*.maze"));
        File file = fileChooser.showOpenDialog(loadButton.getScene().getWindow());

        if (file != null) {
            // Switch to the game scene only if a file was selected
            switchToGameScene();
            // Load the maze using the ViewModel
            viewModel.loadMaze(file);
        }
    }

    /**
     * Helper method to handle the transition from the Welcome screen to the Game screen.
     * Prevents code duplication between onStart and onLoad.
     */
    private void switchToGameScene() {
        try {
            // Load the game screen FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/atpprojectpartc/View/MyView.fxml"));
            Scene gameScene = new Scene(loader.load(), 800, 700);

            // Pass the ViewModel to the game's controller
            MyViewController controller = loader.getController();
            controller.setViewModel(viewModel);

            // Get the current window (Stage) and set the new scene
            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Maze Game");

            // Handle the 'X' button to ensure clean exit
            stage.setOnCloseRequest(event -> {
                event.consume();
                controller.handleExit();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Handles the application exit directly from the welcome screen.
     * Routes the exit command through the ViewModel to stop servers properly.
     */
    public void handleExit() {
        if (viewModel != null) {
            viewModel.exitGame(); // Stops the background servers
        }
        javafx.application.Platform.exit();
        System.exit(0);
    }
}