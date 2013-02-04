package net.fibulwinter.gtd.domain;

public enum TaskStatus {
    NextAction(true),
    Project(true),
    Maybe(false),
    Completed(false),
    Cancelled(false),;


    private boolean active;

    private TaskStatus(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
