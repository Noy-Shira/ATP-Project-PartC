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

public class MyViewModel implements ModelObserver {

    private IModel model;

    // Properties for the View to bind to (Encapsulated)
    private ObjectProperty<Maze> maze = new SimpleObjectProperty<>();
    private ObjectProperty<Position> characterPosition = new SimpleObjectProperty<>();
    private ObjectProperty<Solution> solution = new SimpleObjectProperty<>();
    private StringProperty errorMessage = new SimpleStringProperty();
    private BooleanProperty gameWon = new SimpleBooleanProperty(false);
    private BooleanProperty controlsEnabled = new SimpleBooleanProperty(false);

    public MyViewModel(IModel model) {
        this.model = model;
        this.model.addObserver(this);
    }

    //region Property Getters (The View binds to these properties)
    public ObjectProperty<Maze> mazeProperty() { return maze; }
    public ObjectProperty<Position> characterPositionProperty() { return characterPosition; }
    public ObjectProperty<Solution> solutionProperty() { return solution; }
    public StringProperty errorMessageProperty() { return errorMessage; }
    public BooleanProperty gameWonProperty() { return gameWon; }
    public BooleanProperty controlsEnabledProperty() { return controlsEnabled; }
    //endregion

    //region Commands from View to Model
    public void generateMaze(int rows, int columns) {
        controlsEnabled.set(false);
        model.generateMaze(rows, columns);
    }

    public void solveMaze() {
        model.solveMaze();
    }

    public void moveCharacter(Direction direction) {
        model.moveCharacter(direction);
    }

    public void saveMaze(File file) {
        model.saveMaze(file);
    }

    public void loadMaze(File file) {
        controlsEnabled.set(false);
        model.loadMaze(file);
    }
    //endregion

    //region Notifications from Model (ModelObserver implementation)
    @Override
    public void mazeGenerated() {
        Platform.runLater(() -> {
            maze.set(model.getCurrentMaze());
            characterPosition.set(model.getCharacterPosition());
            gameWon.set(false);
            controlsEnabled.set(true);
        });
    }

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

    @Override
    public void mazeSolved() {
        Platform.runLater(() -> {
            solution.set(model.getCurrentSolution());
        });
    }

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

    @Override
    public void errorOccurred(String message) {
        Platform.runLater(() -> {
            errorMessage.set("");
            errorMessage.set(message);
            controlsEnabled.set(true);
        });
    }
    //endregion

    public void exitGame() {
        model.stopServers();
    }
}