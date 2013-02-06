package net.fibulwinter.gtd.presentation;

public class TaskItemAdapterConfig {
    private boolean showContext = true;
    private boolean showMasterProject = true;
    private boolean showSubActions = false;
    private boolean showStartingDate = false;
    private boolean showDueDate = true;
    private boolean allowChangeStatus = true;
    private boolean showLevel = false;
    private boolean showCompletedDate = false;

    public boolean isShowContext() {
        return showContext;
    }

    public void setShowContext(boolean showContext) {
        this.showContext = showContext;
    }

    public boolean isShowMasterProject() {
        return showMasterProject;
    }

    public void setShowMasterProject(boolean showMasterProject) {
        this.showMasterProject = showMasterProject;
    }

    public boolean isShowSubActions() {
        return showSubActions;
    }

    public void setShowSubActions(boolean showSubActions) {
        this.showSubActions = showSubActions;
    }

    public boolean isShowStartingDate() {
        return showStartingDate;
    }

    public void setShowStartingDate(boolean showStartingDate) {
        this.showStartingDate = showStartingDate;
    }

    public boolean isShowDueDate() {
        return showDueDate;
    }

    public void setShowDueDate(boolean showDueDate) {
        this.showDueDate = showDueDate;
    }

    public boolean isAllowChangeStatus() {
        return allowChangeStatus;
    }

    public void setAllowChangeStatus(boolean allowChangeStatus) {
        this.allowChangeStatus = allowChangeStatus;
    }

    public boolean isShowLevel() {
        return showLevel;
    }

    public void setShowLevel(boolean showLevel) {
        this.showLevel = showLevel;
    }

    public boolean isShowCompletedDate() {
        return showCompletedDate;
    }

    public void setShowCompletedDate(boolean showCompletedDate) {
        this.showCompletedDate = showCompletedDate;
    }
}
