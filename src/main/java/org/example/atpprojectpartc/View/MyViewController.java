package org.example.atpprojectpartc.View;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import org.example.atpprojectpartc.Model.Direction;
import org.example.atpprojectpartc.ViewModel.MyViewModel;
import java.io.File;
import java.util.Optional;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;

public class MyViewController implements IView {

    private MyViewModel viewModel;
    private MediaPlayer backgroundMusic;
    private MediaPlayer winMusic;
    private boolean solutionVisible = false;

    @FXML private MazeDisplayer mazeDisplayer;
    @FXML private BorderPane mainPane;
    @FXML private Pane mazePane;
    @FXML private Button generateButton;
    @FXML private Button solveButton;
    @FXML private MenuItem saveMazeItem;

    //region ViewModel Connection

    public void setViewModel(MyViewModel viewModel) {
        if (viewModel == null) return;
        this.viewModel = viewModel;
        initMusic();
        bindProperties();
    }

    private void initMusic() {
        try {
            // Background music - replays automatically when song ends
            Media bgMedia = new Media(getClass().getResource("/dont_shoot.mp3").toExternalForm());
            backgroundMusic = new MediaPlayer(bgMedia);
            backgroundMusic.setOnEndOfMedia(() -> {
                backgroundMusic.seek(backgroundMusic.getStartTime());
                backgroundMusic.play();
            });

            // Win music - plays once
            Media winMedia = new Media(getClass().getResource("/harvo_darbo.mp3").toExternalForm());
            winMusic = new MediaPlayer(winMedia);

        } catch (Exception e) {
            System.out.println("Warning: Music files not found.");
        }
    }

    private void bindProperties() {
        // Bind maze - also binds canvas size to pane and starts music
        viewModel.mazeProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (winMusic != null) winMusic.stop();

                mazeDisplayer.widthProperty().bind(mazePane.widthProperty());
                mazeDisplayer.heightProperty().bind(mazePane.heightProperty());
                mazeDisplayer.setMaze(newVal);
                solutionVisible = false;
                solveButton.setText("Solve");
                mainPane.requestFocus();

                // Start background music
                if (backgroundMusic != null) {
                    backgroundMusic.stop();
                    backgroundMusic.seek(backgroundMusic.getStartTime());
                    backgroundMusic.play();
                }
            }
        });

        // Bind character position updates
        viewModel.characterPositionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                mazeDisplayer.setCharacterPosition(newVal.getRowIndex(), newVal.getColumnIndex());
        });

        // Bind solution display
        viewModel.solutionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) mazeDisplayer.setSolution(newVal);
        });

        // Bind error messages
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showError(newVal);
                viewModel.errorMessageProperty().set("");
            }
        });

        // Bind win condition - stop background music and play win music
        viewModel.gameWonProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (backgroundMusic != null) backgroundMusic.stop();
                if (winMusic != null) {
                    winMusic.seek(winMusic.getStartTime());
                    winMusic.play();
                }
                showWinMessage();
            }
        });

        // Bind controls enabled/disabled state
        viewModel.controlsEnabledProperty().addListener((obs, oldVal, newVal) ->
                setControlsEnabled(newVal));
    }

    //endregion

    //region File Menu

    @FXML
    private void onNewMaze() {
        // Show dialog to get maze dimensions from user
        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("New Maze");
        dialog.setHeaderText("Enter maze dimensions:");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // Input fields with default values
        TextField rowsField = new TextField("10");
        TextField colsField = new TextField("10");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Rows (2-1000):"), 0, 0);
        grid.add(rowsField, 1, 0);
        grid.add(new Label("Columns (2-1000):"), 0, 1);
        grid.add(colsField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        // Auto-focus rows field when dialog opens
        dialog.setOnShown(e -> rowsField.requestFocus());

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                try {
                    int rows = Integer.parseInt(rowsField.getText().trim());
                    int cols = Integer.parseInt(colsField.getText().trim());
                    if (rows < 2 || cols < 2) {
                        showError("Maze must be at least 2x2!");
                        return null;
                    }
                    if (rows > 1000 || cols > 1000) {
                        showError("Maze cannot be larger than 1000x1000!");
                        return null;
                    }
                    return new int[]{rows, cols};
                } catch (NumberFormatException e) {
                    showError("Please enter valid numbers!");
                    return null;
                }
            }
            return null;
        });

        Optional<int[]> result = dialog.showAndWait();
        result.ifPresent(dims -> {
            if (viewModel != null) {
                mazeDisplayer.hideSolution();
                viewModel.generateMaze(dims[0], dims[1]);
            }
        });
    }

    @FXML
    private void onSaveMaze() {
        if (viewModel == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Maze");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Maze Files", "*.maze"));
        File file = fileChooser.showSaveDialog(mainPane.getScene().getWindow());
        if (file != null) viewModel.saveMaze(file);
    }

    @FXML
    private void onLoadMaze() {
        if (viewModel == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Maze");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Maze Files", "*.maze"));
        File file = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
        if (file != null) {
            mazeDisplayer.hideSolution();
            viewModel.loadMaze(file);
        }
    }

    //endregion

    //region Options / Help / About / Exit Menus

    @FXML
    private void onProperties() {
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(getClass().getResourceAsStream("/config.properties"));

            String content =
                    "Thread Pool Size: " + props.getProperty("threadPoolSize") + "\n" +
                            "Maze Generating Algorithm: " + props.getProperty("mazeGeneratingAlgorithm") + "\n" +
                            "Maze Searching Algorithm: " + props.getProperty("mazeSearchingAlgorithm");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Properties");
            alert.setHeaderText("Game Settings");
            alert.setContentText(content);
            alert.showAndWait();
        } catch (Exception e) {
            showError("Could not load config.properties!");
        }
    }

    @FXML
    private void onHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("How to Play");
        alert.setContentText(
                "Use NumPad to move:\n" +
                        "8 = Up        2 = Down\n" +
                        "4 = Left      6 = Right\n" +
                        "7 = Up-Left   9 = Up-Right\n" +
                        "1 = Down-Left 3 = Down-Right\n\n" +
                        "Ctrl + Scroll = Zoom In/Out\n" +
                        "Drag the player with the mouse!\n\n" +
                        "Reach the Iranian flag to win! \uD83C\uDDEE\uD83C\uDDF1"
        );
        alert.showAndWait();
    }

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Maze Game - Iran Bye Bye");
        alert.setContentText(
                "Developed by: Noy Bohbot & Shira Eshcoli\n\n" +
                        "Maze Generation: MyMazeGenerator\n" +
                        "Maze Solving: BestFirstSearch\n\n" +
                        "Ben-Gurion University of the Negev\n" +
                        "Advanced Topics in Programming - 2026"
        );
        alert.showAndWait();
    }

    @FXML
    private void onExit() {
        // Stop all music before exit
        if (backgroundMusic != null) backgroundMusic.stop();
        if (winMusic != null) winMusic.stop();
        // Clean exit - stop servers
        if (viewModel != null) viewModel.exitGame();
        javafx.application.Platform.exit();
        System.exit(0);
    }

    //endregion

    //region Game Buttons

    @FXML
    private void onGenerateMaze() {
        onNewMaze();
    }

    @FXML
    private void onSolveMaze() {
        if (viewModel == null) return;
        if (!solutionVisible) {
            viewModel.solveMaze();
            solutionVisible = true;
            solveButton.setText("Hide Solution");
        } else {
            mazeDisplayer.hideSolution();
            solutionVisible = false;
            solveButton.setText("Solve");
        }
    }

    //endregion

    //region Keyboard Movement (NumPad)

    @FXML
    private void onKeyPressed(KeyEvent keyEvent) {
        if (viewModel == null) return;
        Direction direction = null;

        switch (keyEvent.getCode()) {
            case NUMPAD8: direction = Direction.UP; break;
            case NUMPAD2: direction = Direction.DOWN; break;
            case NUMPAD4: direction = Direction.LEFT; break;
            case NUMPAD6: direction = Direction.RIGHT; break;
            case NUMPAD7: direction = Direction.UP_LEFT; break;
            case NUMPAD9: direction = Direction.UP_RIGHT; break;
            case NUMPAD1: direction = Direction.DOWN_LEFT; break;
            case NUMPAD3: direction = Direction.DOWN_RIGHT; break;
            default: break;
        }

        if (direction != null) {
            viewModel.moveCharacter(direction);
            keyEvent.consume();
        }
    }

    //endregion

    //region Zoom (Ctrl + Scroll)

    @FXML
    private void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.isControlDown()) {
            double zoomFactor = scrollEvent.getDeltaY() > 0 ? 1.1 : 0.9;
            double newScaleX = mazeDisplayer.getScaleX() * zoomFactor;
            double newScaleY = mazeDisplayer.getScaleY() * zoomFactor;

            // Limit zoom between 0.3x and 5x
            if (newScaleX >= 0.3 && newScaleX <= 5.0) {
                mazeDisplayer.setScaleX(newScaleX);
                mazeDisplayer.setScaleY(newScaleY);
            }
            scrollEvent.consume();
        }
    }

    //endregion

    //region Mouse Drag Movement

    @FXML
    private void onMouseDragged(MouseEvent mouseEvent) {
        if (viewModel == null || viewModel.mazeProperty().get() == null) return;

        Maze maze = viewModel.mazeProperty().get();
        double cellWidth = mazeDisplayer.getWidth() / maze.getCols();
        double cellHeight = mazeDisplayer.getHeight() / maze.getRows();

        if (cellWidth <= 0 || cellHeight <= 0) return;

        int newRow = (int) (mouseEvent.getY() / cellHeight);
        int newCol = (int) (mouseEvent.getX() / cellWidth);

        // Check bounds
        if (newRow < 0 || newRow >= maze.getRows()) return;
        if (newCol < 0 || newCol >= maze.getCols()) return;

        // Check not a wall
        if (maze.getCellValue(newRow, newCol) == 1) return;

        Position current = viewModel.characterPositionProperty().get();
        if (current == null) return;

        // Allow only one step at a time
        int rowDiff = newRow - current.getRowIndex();
        int colDiff = newCol - current.getColumnIndex();
        if (Math.abs(rowDiff) > 1 || Math.abs(colDiff) > 1) return;
        if (rowDiff == 0 && colDiff == 0) return;

        Direction direction = getDirectionFromDiff(rowDiff, colDiff);
        if (direction != null) viewModel.moveCharacter(direction);
    }

    private Direction getDirectionFromDiff(int rowDiff, int colDiff) {
        if (rowDiff == -1 && colDiff ==  0) return Direction.UP;
        if (rowDiff ==  1 && colDiff ==  0) return Direction.DOWN;
        if (rowDiff ==  0 && colDiff == -1) return Direction.LEFT;
        if (rowDiff ==  0 && colDiff ==  1) return Direction.RIGHT;
        if (rowDiff == -1 && colDiff == -1) return Direction.UP_LEFT;
        if (rowDiff == -1 && colDiff ==  1) return Direction.UP_RIGHT;
        if (rowDiff ==  1 && colDiff == -1) return Direction.DOWN_LEFT;
        if (rowDiff ==  1 && colDiff ==  1) return Direction.DOWN_RIGHT;
        return null;
    }

    //endregion

    //region IView Implementation

    @Override
    public void displayMaze(Maze maze) {
        if (maze != null) mazeDisplayer.setMaze(maze);
    }

    @Override
    public void displayCharacterPosition(Position position) {
        if (position != null)
            mazeDisplayer.setCharacterPosition(position.getRowIndex(), position.getColumnIndex());
    }

    @Override
    public void displaySolution(Solution solution) {
        if (solution != null) mazeDisplayer.setSolution(solution);
    }

    @Override
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message != null ? message : "An unknown error occurred.");
        alert.showAndWait();
    }

    @Override
    public void showWinMessage() {
        // Create an independent and styled victory window
        Stage winStage = new Stage();
        winStage.setTitle("ISRAEL WINS!!");
        winStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        VBox layout = new VBox(20);
        layout.setAlignment(javafx.geometry.Pos.CENTER);
        layout.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 20px;");

        try {
            // Load the victory image
            ImageView winImage = new ImageView(
                    new Image(getClass().getResourceAsStream("/final.png")));
            winImage.setFitWidth(600);
            winImage.setPreserveRatio(true);
            layout.getChildren().add(winImage);
        } catch (Exception e) {
            // Fallback in case the image is missing from the resources folder
            Label winLabel = new Label("IRAN BYE BYE!! 🇮🇱");
            winLabel.setStyle("-fx-font-size: 30px; -fx-text-fill: white; -fx-font-weight: bold;");
            layout.getChildren().add(winLabel);
        }

        // Close button to dismiss the window
        Button closeButton = new Button("Play Again");
        closeButton.setOnAction(e -> winStage.close());
        closeButton.setStyle(
                "-fx-background-color: #0f3460;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 10px 30px;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-cursor: hand;"
        );

        layout.getChildren().add(closeButton);

        Scene winScene = new Scene(layout);
        winStage.setScene(winScene);
        winStage.setResizable(false);
        winStage.show();
    }

    @Override
    public void setControlsEnabled(boolean enabled) {
        if (solveButton != null) solveButton.setDisable(!enabled);
        if (saveMazeItem != null) saveMazeItem.setDisable(!enabled);
    }

    public void handleExit() {
        onExit();
    }

    //endregion
}