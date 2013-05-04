package net.fibulwinter.gtd.presentation;

import net.fibulwinter.gtd.domain.Task;

public abstract class StatusTransition {
    private String name;

    protected StatusTransition(String name) {
        this.name = name;
    }

    public abstract void doTransition(Task task);

    @Override
    public String toString() {
        return name;
    }
}
