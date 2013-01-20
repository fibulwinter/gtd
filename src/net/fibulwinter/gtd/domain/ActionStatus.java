package net.fibulwinter.gtd.domain;

public enum ActionStatus {
    NextAction(false),
    Completed(true),
    Cancelled(true),;


    private boolean done;

    private ActionStatus(boolean done) {
        this.done = done;
    }

    public boolean isDone() {
        return done;
    }
}
