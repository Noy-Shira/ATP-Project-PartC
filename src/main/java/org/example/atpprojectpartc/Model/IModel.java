package org.example.atpprojectpartc.Model;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;
import java.io.File;

public interface IModel {
    void generateMaze(int rows, int columns);
    void solveMaze();
    void moveCharacter(Direction direction);
    void saveMaze(File file);
    void loadMaze(File file);
    Maze getCurrentMaze();
    Position getCharacterPosition();
    Solution getCurrentSolution();
    void addObserver(ModelObserver observer);
    void removeObserver(ModelObserver observer);
}