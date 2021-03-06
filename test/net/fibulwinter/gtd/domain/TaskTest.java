package net.fibulwinter.gtd.domain;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

@Test
public class TaskTest {
    private static final String TEXT = "Text";
    public static final String SUB_ACTION_TEXT = "Sub1";

    private Task task;

    @BeforeMethod
    public void givenAction() {
        task = new Task(TEXT);
    }

    public void itShouldHaveText() {
        assertThat(task.getText(), is(TEXT));
    }

    public void itShouldBeNextActionByDefault() {
        assertThat(task.getStatus(), is(TaskStatus.NextAction));
    }

    public void itShouldHaveAssignableStatus() {
        task.setStatus(TaskStatus.Completed);

        assertThat(task.getStatus(), is(TaskStatus.Completed));
    }

    public void itShouldBeCompletedAfterComplete() {
        task.complete();

        assertThat(task.getStatus(), is(TaskStatus.Completed));
    }

    public void itShouldBeCancelledAfterCancel() {
        task.cancel();

        assertThat(task.getStatus(), is(TaskStatus.Cancelled));
    }

    public void itShouldHaveSubActions() {
        Task sub1 = new Task(SUB_ACTION_TEXT);
        sub1.setMaster(task);

        assertThat(task.getSubTasks(), contains(sub1));
    }

    public void itShouldHaveMasterAction() {
        Task sub1 = new Task(SUB_ACTION_TEXT);
        sub1.setMaster(task);

        assertThat(sub1.getMasterTask().get(), is(task));
    }

    public void itShouldNotHaveMasterActionByDefault() {
        assertThat(task.getMasterTask().isPresent(), is(false));
    }

    public void itShouldBeNotProjectByDefault() {
        assertThat(task.isProject(), is(false));
    }

    public void itShouldBeProjectAfterSubActionWasAdded() {
        Task sub1 = new Task(SUB_ACTION_TEXT);
        sub1.setMaster(task);

        assertThat(task.isProject(), is(true));
    }

    public void subActionShouldBeCancelledWhenMasterIsCancelled() {
        Task subTask = new Task(SUB_ACTION_TEXT);
        subTask.setMaster(task);
        task.cancel();

        assertThat(subTask.getStatus(), is(TaskStatus.Cancelled));
    }

    public void subActionShouldBeCancelledWhenMasterIsComplete() {
        Task subTask = new Task(SUB_ACTION_TEXT);
        subTask.setMaster(task);
        task.complete();

        assertThat(subTask.getStatus(), is(TaskStatus.Cancelled));
    }

    public void completeSubActionShouldBeCompletedWhenMasterIsCancelled() {
        Task subTask = new Task(SUB_ACTION_TEXT);
        subTask.setMaster(task);
        subTask.complete();
        task.cancel();

        assertThat(subTask.getStatus(), is(TaskStatus.Completed));
    }

    public void subActionShouldHaveOriginalStatusWhenMasterIsActiveAgain() {
        Task subTask = new Task(SUB_ACTION_TEXT);
        subTask.setMaster(task);
        task.cancel();
        task.setStatus(TaskStatus.NextAction);

        assertThat(subTask.getStatus(), is(TaskStatus.NextAction));
    }


}
