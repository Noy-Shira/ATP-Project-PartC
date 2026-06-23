package org.example.atpprojectpartc.Model;

import IO.MyCompressorOutputStream;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;
import Server.Server;
import Server.ServerStrategyGenerateMaze;
import Server.ServerStrategySolveSearchProblem;
import IO.MyDecompressorInputStream;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of the IModel interface.
 * Manages the application's state, runs background servers for heavy computations,
 * and handles client-server communications to generate and solve mazes.
 */
public class MyModel implements IModel {

    // Ports used to connect to the two servers created in Part B.
    private static final int MAZE_GENERATING_PORT = 5400;
    private static final int SOLVE_SEARCH_PROBLEM_PORT = 5401;
    private static final int LISTENING_INTERVAL_MS = 1000;

    // The maze the user is currently playing on.
    private Maze currentMaze;

    // The current position of the character inside the maze.
    private Position characterPosition;

    // The last solution received from the solving server (if any).
    private Solution currentSolution;

    // List of listeners that want to know when something changes in the model.
    private final List<ModelObserver> observers = new ArrayList<>();

    // The two servers that this application starts and uses internally.
    private Server mazeGeneratingServer;
    private Server solveSearchProblemServer;

    /**
     * Constructor for MyModel.
     * Automatically initializes and starts the backend servers upon creation.
     */
    public MyModel() {
        startServers();
    }

    /**
     * Starts both servers so the model can later connect to them as a client.
     */
    private void startServers() {
        mazeGeneratingServer = new Server(MAZE_GENERATING_PORT, LISTENING_INTERVAL_MS, new ServerStrategyGenerateMaze());
        solveSearchProblemServer = new Server(SOLVE_SEARCH_PROBLEM_PORT, LISTENING_INTERVAL_MS, new ServerStrategySolveSearchProblem());
        mazeGeneratingServer.start();
        solveSearchProblemServer.start();
    }

    /**
     * Should be called when the application closes, to shut down both servers cleanly.
     */
    @Override
    public void stopServers() {
        mazeGeneratingServer.stop();
        solveSearchProblemServer.stop();
    }

    //region Observer management (lets ViewModel know when something changes)

    @Override
    public void addObserver(ModelObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(ModelObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all registered observers that a new maze was generated.
     */
    private void notifyMazeGenerated() {
        for (ModelObserver o : observers) o.mazeGenerated();
    }

    /**
     * Notifies all registered observers that a maze was loaded from a file.
     */
    private void notifyMazeLoaded() {
        for (ModelObserver o : observers) o.mazeLoaded();
    }

    /**
     * Notifies all registered observers that the maze was solved.
     */
    private void notifyMazeSolved() {
        for (ModelObserver o : observers) o.mazeSolved();
    }

    /**
     * Notifies all registered observers that the character moved.
     */
    private void notifyCharacterMoved() {
        for (ModelObserver o : observers) o.characterMoved();
    }

    /**
     * Notifies all registered observers that an error happened.
     * @param message The error message to broadcast.
     */
    private void notifyError(String message) {
        for (ModelObserver o : observers) o.errorOccurred(message);
    }

    //endregion

    /**
     * Asks the maze-generating server for a new maze, on a separate thread
     * so the GUI does not freeze while waiting for the server's response.
     * @param rows The desired number of rows.
     * @param columns The desired number of columns.
     */
    @Override
    public void generateMaze(int rows, int columns) {
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", MAZE_GENERATING_PORT);
                ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
                toServer.flush();

                int[] mazeDimensions = new int[]{rows, columns};
                toServer.writeObject(mazeDimensions);
                toServer.flush();

                byte[] compressedMaze = (byte[]) fromServer.readObject();

                // MyDecompressorInputStream is designed to be called exactly once,
                // with a buffer sized to the exact decompressed maze length
                // (12 header bytes + one byte per cell) - same usage pattern as
                // tested in Part B's RunCompressDecompressMaze.
                int decompressedSize = 12 + rows * columns;
                byte[] decompressedMaze = new byte[decompressedSize];

                InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                is.read(decompressedMaze);
                is.close();

                currentMaze = new Maze(decompressedMaze);
                characterPosition = currentMaze.getStartPosition();

                socket.close();
                notifyMazeGenerated();
            } catch (Exception e) {
                notifyError("Failed to generate maze: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Asks the solving server to solve the current maze, on a separate thread.
     */
    @Override
    public void solveMaze() {
        if (currentMaze == null) {
            notifyError("There is no maze to solve. Generate a maze first.");
            return;
        }
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", SOLVE_SEARCH_PROBLEM_PORT);
                ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
                toServer.flush();

                // Send the current maze to the server.
                toServer.writeObject(currentMaze);
                toServer.flush();

                // Receive the solution back from the server.
                Solution solution = (Solution) fromServer.readObject();
                currentSolution = solution;

                socket.close();
                notifyMazeSolved();
            } catch (Exception e) {
                notifyError("Failed to solve maze: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Moves the character one step in the given direction, if the move is legal.
     * Checks maze boundaries, walls, and for diagonal moves, applies the same
     * "no corner-cutting" rule used in Part A's SearchableMaze.
     * @param direction The requested direction of movement.
     */
    @Override
    public void moveCharacter(Direction direction) {
        if (currentMaze == null || characterPosition == null) {
            notifyError("There is no maze to move in. Generate a maze first.");
            return;
        }

        int currentRow = characterPosition.getRowIndex();
        int currentCol = characterPosition.getColumnIndex();

        int rowDelta = 0;
        int colDelta = 0;

        // Translate the requested direction into a row/column offset.
        switch (direction) {
            case UP:         rowDelta = -1; colDelta = 0;  break;
            case DOWN:       rowDelta = 1;  colDelta = 0;  break;
            case LEFT:       rowDelta = 0;  colDelta = -1; break;
            case RIGHT:      rowDelta = 0;  colDelta = 1;  break;
            case UP_LEFT:    rowDelta = -1; colDelta = -1; break;
            case UP_RIGHT:   rowDelta = -1; colDelta = 1;  break;
            case DOWN_LEFT:  rowDelta = 1;  colDelta = -1; break;
            case DOWN_RIGHT: rowDelta = 1;  colDelta = 1;  break;
        }

        int newRow = currentRow + rowDelta;
        int newCol = currentCol + colDelta;

        // Check the target cell is inside the maze.
        if (newRow < 0 || newRow >= currentMaze.getRows() || newCol < 0 || newCol >= currentMaze.getCols()) {
            notifyError("You cannot move outside the maze.");
            return;
        }

        // Check the target cell itself is not a wall.
        if (currentMaze.getCellValue(newRow, newCol) == 1) {
            notifyError("There is a wall in that direction.");
            return;
        }

        // For diagonal moves: allowed if at least one of the two "L-shaped" paths
        // (row-first or column-first) is open. Blocked only if BOTH are walls -
        // same rule used in Part A's SearchableMaze.
        boolean isDiagonal = (rowDelta != 0 && colDelta != 0);
        if (isDiagonal) {
            boolean rowNeighborOpen = currentMaze.getCellValue(newRow, currentCol) == 0;
            boolean colNeighborOpen = currentMaze.getCellValue(currentRow, newCol) == 0;
            if (!rowNeighborOpen && !colNeighborOpen) {
                notifyError("You cannot cut through that corner.");
                return;
            }
        }

        characterPosition = new Position(newRow, newCol);
        notifyCharacterMoved();
    }

    /**
     * Saves the current maze to the given file, compressed using the same
     * compressor written in Part B (MyCompressorOutputStream).
     * @param file The file to save the maze to.
     */
    @Override
    public void saveMaze(File file) {
        if (currentMaze == null) {
            notifyError("There is no maze to save. Generate a maze first.");
            return;
        }
        try {
            OutputStream out = new MyCompressorOutputStream(new FileOutputStream(file));
            out.write(currentMaze.toByteArray());
            out.flush();
            out.close();
        } catch (IOException e) {
            notifyError("Failed to save maze: " + e.getMessage());
        }
    }

    /**
     * Loads a maze from the given file, decompresses it, and rebuilds the Maze object.
     * The character is placed back at the maze's start position.
     * @param file The file to load the maze from.
     */
    @Override
    public void loadMaze(File file) {
        new Thread(() -> {
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] compressedBytes = fis.readAllBytes();
                fis.close();

                byte[] decompressedMaze = new byte[12 + 1000 * 1000];
                InputStream in = new MyDecompressorInputStream(
                        new ByteArrayInputStream(compressedBytes));
                in.read(decompressedMaze);
                in.close();

                currentMaze = new Maze(decompressedMaze);
                characterPosition = currentMaze.getStartPosition();
                notifyMazeLoaded();

            } catch (Exception e) {
                e.printStackTrace();
                notifyError("Failed to load maze: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public Maze getCurrentMaze() {
        return currentMaze;
    }

    @Override
    public Position getCharacterPosition() {
        return characterPosition;
    }

    @Override
    public Solution getCurrentSolution() {
        return currentSolution;
    }
}