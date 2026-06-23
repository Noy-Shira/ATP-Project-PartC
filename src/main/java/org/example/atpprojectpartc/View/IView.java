package org.example.atpprojectpartc.View;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;

/**
 * Interface defining the essential view operations for the Maze application.
 * Any class implementing this interface must provide concrete implementations
 * for displaying game elements and interacting with the user interface.
 */
public interface IView {
    /**
     * Displays the given maze on the screen.
     * * @param maze The Maze object to be displayed.
     */
    void displayMaze(Maze maze);
    /**
     * Updates and displays the character's current position within the maze.
     * * @param position The current Position of the character.
     */
    void displayCharacterPosition(Position position);
    /**
     * Displays the solution path from the current position to the goal.
     * * @param solution The Solution object containing the path.
     */
    void displaySolution(Solution solution);
    /**
     * Prompts the user with an error message dialog.
     * * @param message The error message to be displayed.
     */
    void showError(String message);
    /**
     * Triggers the victory message or animation when the user reaches the goal.
     */
    void showWinMessage();
    /**
     * Enables or disables the UI controls (e.g., buttons, menu items).
     * Useful for preventing user input while calculations are running.
     * * @param enabled True to enable controls, false to disable them.
     */
    void setControlsEnabled(boolean enabled);
}