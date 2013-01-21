package net.fibulwinter.gtd.domain;

public enum TaskStatus {
    NextAction(false),
    Completed(true),
    Cancelled(true),;


    private boolean done;

    private TaskStatus(boolean done) {
        this.done = done;
    }

    public boolean isDone() {
        return done;
    }
}
