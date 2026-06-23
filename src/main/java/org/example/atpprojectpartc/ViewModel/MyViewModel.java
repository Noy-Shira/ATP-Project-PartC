package org.example.atpprojectpartc.ViewModel;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;
import javafx.application.Platform;
import javafx.beans.property.*;
import org.example.atpprojectpartc.Model.Direction;
import org.example.atpprojectpartc.Model.IModel;
import org.example.atpprojectpartc.Model.ModelObserver;
import java.io.File;

/**
 * ViewModel in the MVVM architecture.
 * Bridges the View and the Model, handling UI commands, delegating work to the Model,
 * and exposing observable properties for the View to bind to.
 */
public class MyViewModel implements ModelObserver {

    private IModel model;

    // Properties for the View to bind to (Encapsulated)
    private ObjectProperty<Maze> maze = new SimpleObjectProperty<>();
    private ObjectProperty<Position> characterPosition = new SimpleObjectProperty<>();
    private ObjectProperty<Solution> solution = new SimpleObjectProperty<>();
    private StringProperty errorMessage = new SimpleStringProperty();
    private BooleanProperty gameWon = new SimpleBooleanProperty(false);
    private BooleanProperty controlsEnabled = new SimpleBooleanProperty(false);

    /**
     * Constructor for MyViewModel.
     * @param model The IModel implementation to interact with.
     */
    public MyViewModel(IModel model) {
        this.model = model;
        this.model.addObserver(this);
    }

    //region Property Getters (The View binds to these properties)

    /** @return The observable property containing the current Maze. */
    public ObjectProperty<Maze> mazeProperty() { return maze; }

    /** @return The observable property containing the character's current Position. */
    public ObjectProperty<Position> characterPositionProperty() { return characterPosition; }

    /** @return The observable property containing the calculated Solution. */
    public ObjectProperty<Solution> solutionProperty() { return solution; }

    /** @return The observable property containing any error messages. */
    public StringProperty errorMessageProperty() { return errorMessage; }

    /** @return The observable property representing the win state of the game. */
    public BooleanProperty gameWonProperty() { return gameWon; }

    /** @return The observable property dictating whether UI controls should be enabled. */
    public BooleanProperty controlsEnabledProperty() { return controlsEnabled; }

    //endregion

    //region Commands from View to Model

    /**
     * Commands the model to generate a new maze. Disables UI controls during generation.
     * @param rows The number of rows.
     * @param columns The number of columns.
     */
    public void generateMaze(int rows, int columns) {
        controlsEnabled.set(false);
        model.generateMaze(rows, columns);
    }

    /**
     * Commands the model to solve the current maze.
     */
    public void solveMaze() {
        model.solveMaze();
    }

    /**
     * Commands the model to move the character in a specific direction.
     * @param direction The desired movement direction.
     */
    public void moveCharacter(Direction direction) {
        model.moveCharacter(direction);
    }

    /**
     * Commands the model to save the current maze to a file.
     * @param file The destination file.
     */
    public void saveMaze(File file) {
        model.saveMaze(file);
    }

    /**
     * Commands the model to load a maze from a file. Disables UI controls during loading.
     * @param file The source file.
     */
    public void loadMaze(File file) {
        controlsEnabled.set(false);
        model.loadMaze(file);
    }
    //endregion

    //region Notifications from Model (ModelObserver implementation)

    /**
     * Called by the Model when a maze finishes generating.
     * Updates JavaFX properties on the JavaFX Application Thread.
     */
    @Override
    public void mazeGenerated() {
        Platform.runLater(() -> {
            maze.set(model.getCurrentMaze());
            characterPosition.set(model.getCharacterPosition());
            gameWon.set(false);
            controlsEnabled.set(true);
        });
    }

    /**
     * Called by the Model when a maze finishes loading from a file.
     * Updates JavaFX properties on the JavaFX Application Thread.
     */
    @Override
    public void mazeLoaded() {
        Platform.runLater(() -> {
            maze.set(null);
            maze.set(model.getCurrentMaze());
            characterPosition.set(model.getCharacterPosition());
            gameWon.set(false);
            controlsEnabled.set(true);
        });
    }

    /**
     * Called by the Model when a solution finishes calculating.
     * Updates the solution property on the JavaFX Application Thread.
     */
    @Override
    public void mazeSolved() {
        Platform.runLater(() -> {
            solution.set(model.getCurrentSolution());
        });
    }

    /**
     * Called by the Model when the character successfully moves.
     * Also checks if the character has reached the goal to trigger a win state.
     */
    @Override
    public void characterMoved() {
        Platform.runLater(() -> {
            Position pos = model.getCharacterPosition();
            characterPosition.set(pos);

            // Check for win condition: comparing row and column indices manually
            Maze currentMaze = model.getCurrentMaze();
            if (currentMaze != null) {
                Position goal = currentMaze.getGoalPosition();
                if (pos.getRowIndex() == goal.getRowIndex() && pos.getColumnIndex() == goal.getColumnIndex()) {
                    gameWon.set(true);
                    controlsEnabled.set(false);
                }
            }
        });
    }

    /**
     * Called by the Model when an error occurs.
     * Relays the error message to the View on the JavaFX Application Thread.
     * @param message The error message.
     */
    @Override
    public void errorOccurred(String message) {
        Platform.runLater(() -> {
            errorMessage.set("");
            errorMessage.set(message);
            controlsEnabled.set(true);
        });
    }
    //endregion

    /**
     * Exits the game cleanly by shutting down the Model's servers.
     */
    public void exitGame() {
        model.stopServers();
    }
}