package org.example.atpprojectpartc.Model;

public interface ModelObserver {
    void mazeGenerated();
    void mazeSolved();
    void characterMoved();
    void mazeLoaded();
}