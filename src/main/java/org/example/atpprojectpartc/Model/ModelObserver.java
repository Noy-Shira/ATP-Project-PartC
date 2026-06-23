package org.example.atpprojectpartc.Model;

/**
 * Interface defining the callback methods for observing changes in the Model.
 * Implemented by the ViewModel to react to background tasks completed by the Model.
 */
public interface ModelObserver {
    /**
     * Triggered when a new maze has been successfully generated.
     */
    void mazeGenerated();

    /**
     * Triggered when a solution for the current maze has been successfully calculated.
     */
    void mazeSolved();

    /**
     * Triggered when the character's position has been successfully updated.
     */
    void characterMoved();

    /**
     * Triggered when a maze has been successfully loaded from a file.
     */
    void mazeLoaded();

    /**
     * Triggered when an error occurs within the model's operations.
     * @param message A descriptive error message.
     */
    void errorOccurred(String message);
}