/**
 * Filename: Orange.java
 * Author: Nate Williams
 * Due Date: Feb 21, 2025
 * 
 * This is our Orange class that is processed by the Plant and Worker classes.
 */

public class Orange {
    // List all of the states and how long they take to complete
    /**
     * Enumerates all the possible States of the Orange as well as how long they take to complete
     */
    public enum State {
        Fetched(15), // Retrieve the orange (takes 15 ms)
        Peeled(38), // Peel the orange (takes 38 ms)
        Squeezed(29), // Juice the orange (takes 29 ms)
        Bottled(17), // Bottle the juice (takes 17 ms)
        Processed(1); // Complete the orange juicing (takes 1 ms)

        private static final int finalIndex = State.values().length - 1;

        final int timeToComplete;

        State(int timeToComplete) {
            this.timeToComplete = timeToComplete;
        }

        State getNext() {
            int currIndex = this.ordinal();
            if (currIndex >= finalIndex) {
                throw new IllegalStateException("Already at final state");
            }
            return State.values()[currIndex + 1];
        }
    }

    private State state;

    // Initialize Orange
    public Orange() {
        state = State.Fetched;
        doWork();
    }

    // Getter for state of Orange
    /**
     * @return Current state of the Orange
     */
    public State getState() {
        return state;
    }

    // Advance to the next State (process the Orange one step further)
    /**
     * Advance the Orange to the next State
     */
    public void runProcess() {
        // Don't attempt to process an already completed orange
        if (state == State.Processed) {
            throw new IllegalStateException("This orange has already been processed");
        }
        doWork();
        state = state.getNext();
    }

    /**
     * Make the Thread that is working on the Orange sleep until the Orange finishes its process
     */
    private void doWork() {
        // Sleep for the amount of time necessary to do the work
        try {
            Thread.sleep(state.timeToComplete);
        } catch (InterruptedException e) {
            System.err.println("Incomplete orange processing, juice may be bad");
        }
    }
}
