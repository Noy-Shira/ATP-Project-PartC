package org.example.atpprojectpartc.View;

import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import algorithms.search.AState;
import algorithms.search.MazeState;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.ArrayList;

/**
 * Custom JavaFX Canvas component responsible for rendering the maze,
 * the character, the goal, and the solution path.
 */
public class MazeDisplayer extends Canvas {

    private Maze maze;
    private int characterRow;
    private int characterCol;
    private Solution solution;
    private boolean showSolution = false;

    // Image properties
    private Image playerImage;
    private Image goalImage;

    /**
     * Constructor for MazeDisplayer.
     * Initializes the player and goal images and sets up listeners
     * to redraw the canvas dynamically when its dimensions change.
     */
    public MazeDisplayer() {
        // Attempt to load images safely
        try {
            playerImage = new Image(getClass().getResourceAsStream("/israel.png"));
            goalImage = new Image(getClass().getResourceAsStream("/iran.png"));
        } catch (Exception e) {
            System.out.println("Warning: Images not found. Using default colors.");
        }

        // Trigger a redraw whenever the Canvas dimensions change
        widthProperty().addListener(e -> draw());
        heightProperty().addListener(e -> draw());
    }

    /**
     * Sets a new maze to be displayed and resets the character position to the start.
     * * @param maze The new Maze object to display.
     */
    public void setMaze(Maze maze) {
        this.maze = maze;
        this.showSolution = false;
        this.solution = null;
        if (maze != null) {
            // Set the initial character position based on the maze's start position
            this.characterRow = maze.getStartPosition().getRowIndex();
            this.characterCol = maze.getStartPosition().getColumnIndex();
        }
        draw();
    }

    /**
     * Updates the character's position on the canvas and triggers a redraw.
     * * @param row The new row index of the character.
     * @param col The new column index of the character.
     */
    public void setCharacterPosition(int row, int col) {
        this.characterRow = row;
        this.characterCol = col;
        draw();
    }

    /**
     * Sets the solution path to be displayed and triggers a redraw.
     * * @param solution The Solution object containing the path to the goal.
     */
    public void setSolution(Solution solution) {
        this.solution = solution;
        this.showSolution = true;
        draw();
    }

    /**
     * Hides the currently displayed solution path and triggers a redraw.
     */
    public void hideSolution() {
        this.showSolution = false;
        draw();
    }

    /**
     * Core rendering logic. Clears the canvas and draws the maze walls,
     * paths, solution (if enabled), goal, and character based on the current dimensions.
     */
    private void draw() {
        if (maze == null) return;

        GraphicsContext gc = getGraphicsContext2D();
        double width = getWidth();
        double height = getHeight();

        // Edge case: Ensure the Canvas has valid dimensions before drawing
        if (width <= 0 || height <= 0) return;

        double cellWidth = width / maze.getCols();
        double cellHeight = height / maze.getRows();

        // Edge case: Prevent division by zero or invalid cell sizes
        if (cellWidth <= 0 || cellHeight <= 0) return;

        // Clear the canvas prior to redrawing
        gc.clearRect(0, 0, width, height);

        // Draw the maze layout
        for (int row = 0; row < maze.getRows(); row++) {
            for (int col = 0; col < maze.getCols(); col++) {
                if (maze.getCellValue(row, col) == 1) {
                    gc.setFill(Color.BLACK); // Wall
                } else {
                    gc.setFill(Color.WHITE); // Path
                }
                gc.fillRect(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
            }
        }

        // Draw the solution path if requested
        if (showSolution && solution != null) {
            gc.setFill(Color.LIGHTBLUE);
            ArrayList<AState> solutionPath = solution.getSolutionPath();
            for (AState state : solutionPath) {
                if (state instanceof MazeState) {
                    MazeState mazeState = (MazeState) state;
                    int row = mazeState.getRow();
                    int col = mazeState.getCol();
                    gc.fillRect(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
                }
            }
        }

        // Draw the goal position
        int goalRow = maze.getGoalPosition().getRowIndex();
        int goalCol = maze.getGoalPosition().getColumnIndex();
        if (goalImage != null && !goalImage.isError()) {
            gc.drawImage(goalImage, goalCol * cellWidth, goalRow * cellHeight, cellWidth, cellHeight);
        } else {
            gc.setFill(Color.RED); // Fallback color
            gc.fillOval(goalCol * cellWidth, goalRow * cellHeight, cellWidth, cellHeight);
        }

        // Draw the character
        if (playerImage != null && !playerImage.isError()) {
            gc.drawImage(playerImage, characterCol * cellWidth, characterRow * cellHeight, cellWidth, cellHeight);
        } else {
            gc.setFill(Color.BLUE); // Fallback color
            gc.fillOval(characterCol * cellWidth, characterRow * cellHeight, cellWidth, cellHeight);
        }
    }

    // Overridden methods to allow the Canvas to resize dynamically within its layout container
    /**
     * @return true to indicate that this Canvas is resizable.
     */
    @Override
    public boolean isResizable() { return true; }

    /**
     * @param height The height.
     * @return The preferred width based on the current canvas width.
     */
    @Override
    public double prefWidth(double height) { return getWidth(); }

    /**
     * @param width The width.
     * @return The preferred height based on the current canvas height.
     */
    @Override
    public double prefHeight(double width) { return getHeight(); }
}