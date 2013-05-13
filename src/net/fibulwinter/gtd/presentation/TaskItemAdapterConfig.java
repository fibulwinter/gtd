package net.fibulwinter.gtd.presentation;

public class TaskItemAdapterConfig {
    private boolean showContext = true;
    private boolean showMasterProject = true;
    private boolean showSubActions = false;
    private boolean showFutureStartingDate = true;
    private boolean showDueDate = true;
    private boolean showTimeConstraints = false;
    private boolean allowChangeStatus = true;
    private boolean showLevel = false;
    private boolean showCompletedDate = false;
    private boolean editMode = false;

    public static TaskItemAdapterConfig list() {
        return new TaskItemAdapterConfig();
    }

    public static TaskItemAdapterConfig blockedList() {
        TaskItemAdapterConfig config = new TaskItemAdapterConfig();
        config.showSubActions = true;
        return config;
    }

    public static TaskItemAdapterConfig searchList() {
        TaskItemAdapterConfig config = new TaskItemAdapterConfig();
        config.allowChangeStatus = false;
        return config;
    }

    public static TaskItemAdapterConfig logList() {
        TaskItemAdapterConfig config = new TaskItemAdapterConfig();
        config.showContext = false;
        config.showFutureStartingDate = false;
        config.showDueDate = false;
        config.allowChangeStatus = false;
        config.showCompletedDate = true;
        return config;
    }

    public static TaskItemAdapterConfig projectView() {
        TaskItemAdapterConfig config = new TaskItemAdapterConfig();
        config.showMasterProject = false;
        config.showFutureStartingDate = false;
        config.showDueDate = false;
        config.showTimeConstraints = true;
        config.showLevel = true;
        return config;
    }

    public static TaskItemAdapterConfig editProjectView() {
        TaskItemAdapterConfig config = new TaskItemAdapterConfig();
        config.showContext = false;
        config.showMasterProject = false;
        config.showFutureStartingDate = false;
        config.showDueDate = false;
        config.showTimeConstraints = false;
        config.editMode = true;
        config.showLevel = true;
        return config;
    }

    private TaskItemAdapterConfig() {
    }

    public boolean isShowTimeConstraints() {
        return showTimeConstraints;
    }

    public boolean isShowContext() {
        return showContext;
    }

    public boolean isShowMasterProject() {
        return showMasterProject;
    }

    public boolean isShowSubActions() {
        return showSubActions;
    }

    public boolean isShowFutureStartingDate() {
        return showFutureStartingDate;
    }

    public boolean isShowDueDate() {
        return showDueDate;
    }

    public boolean isAllowChangeStatus() {
        return allowChangeStatus;
    }

    public boolean isShowLevel() {
        return showLevel;
    }

    public boolean isShowCompletedDate() {
        return showCompletedDate;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setShowContext(boolean showContext) {
        this.showContext = showContext;
    }
}
