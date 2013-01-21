package net.fibulwinter.gtd.domain;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@Test
public class TaskRepositoryTest {
    private TaskRepository taskRepository;

    @BeforeMethod
    public void givenActionList() {
        taskRepository = new TaskRepository(null);
    }

    public void itShouldHaveActionWhenActionWasSaved() {
        Task task = new Task("Text");
        taskRepository.save(task);

        assertThat(taskRepository.getAll(), contains(task));
    }


}
