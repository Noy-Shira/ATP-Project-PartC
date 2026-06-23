package org.example.atpprojectpartc.Model;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;
import java.io.File;

/**
 * Interface defining the core business logic and state management for the Maze application.
 * Defines the contract for generating, solving, saving, and loading mazes,
 * as well as managing the character's state.
 */
public interface IModel {
    /**
     * Requests the generation of a new maze with the specified dimensions.
     * @param rows The number of rows for the new maze.
     * @param columns The number of columns for the new maze.
     */
    void generateMaze(int rows, int columns);

    /**
     * Requests the calculation of a solution path for the current maze.
     */
    void solveMaze();

    /**
     * Attempts to move the character one step in the specified direction.
     * @param direction The requested Direction of movement.
     */
    void moveCharacter(Direction direction);

    /**
     * Saves the current maze state to a specified file.
     * @param file The File object representing the destination file.
     */
    void saveMaze(File file);

    /**
     * Loads a previously saved maze from a specified file.
     * @param file The File object representing the source file to load from.
     */
    void loadMaze(File file);

    /**
     * @return The currently active Maze object.
     */
    Maze getCurrentMaze();

    /**
     * @return The current Position of the character within the maze.
     */
    Position getCharacterPosition();

    /**
     * @return The Solution object representing the path to the goal, or null if not yet solved.
     */
    Solution getCurrentSolution();

    /**
     * Registers an observer to listen for model state changes.
     * @param observer The ModelObserver to add.
     */
    void addObserver(ModelObserver observer);

    /**
     * Unregisters an observer so it no longer receives updates.
     * @param observer The ModelObserver to remove.
     */
    void removeObserver(ModelObserver observer);

    /**
     * Gracefully stops the backend servers used for maze generation and solving.
     */
    void stopServers();
}