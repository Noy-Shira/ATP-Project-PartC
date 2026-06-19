package org.example.atpprojectpartc.View;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;

public interface IView {
    void displayMaze(Maze maze);

    void displayCharacterPosition(Position position);

    void displaySolution(Solution solution);

    void showError(String message);

    void showWinMessage();

    void setControlsEnabled(boolean enabled);
}