package net.fibulwinter.gtd.domain;

import com.google.common.base.Optional;
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
        assertThat(task.getStatus(), is(ActionStatus.NextAction));
    }

    public void itShouldHaveAssignableStatus() {
        task.setStatus(ActionStatus.Completed);

        assertThat(task.getStatus(), is(ActionStatus.Completed));
    }

    public void itShouldBeCompletedAfterComplete() {
        task.complete();

        assertThat(task.getStatus(), is(ActionStatus.Completed));
    }

    public void itShouldBeCancelledAfterCancel() {
        task.cancel();

        assertThat(task.getStatus(), is(ActionStatus.Cancelled));
    }

    public void itShouldHaveSubActions() {
        Task sub1 = new Task(SUB_ACTION_TEXT, Optional.of(task));

        assertThat(task.getSubTasks(), contains(sub1));
    }

    public void itShouldHaveMasterAction() {
        assertThat(new Task(SUB_ACTION_TEXT, Optional.of(task)).getMasterAction().get(), is(task));
    }

    public void itShouldNotHaveMasterActionByDefault() {
        assertThat(task.getMasterAction().isPresent(), is(false));
    }

    public void itShouldBeNotProjectByDefault() {
        assertThat(task.isProject(), is(false));
    }

    public void itShouldBeProjectAfterSubActionWasAdded() {
        new Task(SUB_ACTION_TEXT, Optional.of(task));

        assertThat(task.isProject(), is(true));
    }

    public void subActionShouldBeCancelledWhenMasterIsCancelled() {
        Task subTask = new Task(SUB_ACTION_TEXT, Optional.of(task));
        task.cancel();

        assertThat(subTask.getStatus(), is(ActionStatus.Cancelled));
    }

    public void subActionShouldBeCancelledWhenMasterIsComplete() {
        Task subTask = new Task(SUB_ACTION_TEXT, Optional.of(task));
        task.complete();

        assertThat(subTask.getStatus(), is(ActionStatus.Cancelled));
    }

    public void completeSubActionShouldBeCompletedWhenMasterIsCancelled() {
        Task subTask = new Task(SUB_ACTION_TEXT, Optional.of(task));
        subTask.complete();
        task.cancel();

        assertThat(subTask.getStatus(), is(ActionStatus.Completed));
    }

    public void subActionShouldHaveOriginalStatusWhenMasterIsActiveAgain() {
        Task subTask = new Task(SUB_ACTION_TEXT, Optional.of(task));
        task.cancel();
        task.setStatus(ActionStatus.NextAction);

        assertThat(subTask.getStatus(), is(ActionStatus.NextAction));
    }


}
