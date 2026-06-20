package org.example.atpprojectpartc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.atpprojectpartc.Model.MyModel;
import org.example.atpprojectpartc.View.WelcomeController;
import org.example.atpprojectpartc.ViewModel.MyViewModel;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Create the Model
        MyModel model = new MyModel();

        // 2. Create the ViewModel with the Model
        MyViewModel viewModel = new MyViewModel(model);

        // 3. Load the Welcome Screen FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/example/atpprojectpartc/View/WelcomeView.fxml"));
        Scene scene = new Scene(loader.load(), 600, 600);

        // 4. Pass the ViewModel to the WelcomeController
        WelcomeController controller = loader.getController();
        controller.setViewModel(viewModel);

        // 5. Handle X button - clean exit without code duplication
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // prevent default close
            controller.handleExit(); // Route through controller, just like the original!
        });

        // 6. Setup and display the window
        primaryStage.setTitle("Maze Game - Israel vs Iran");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}