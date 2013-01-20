package net.fibulwinter.gtd.domain;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@Test
public class ActionRepositoryTest {
    private ActionRepository actionRepository;

    @BeforeMethod
    public void givenActionList() {
        actionRepository = new ActionRepository();
    }

    public void itShouldHaveActionWhenActionWasSaved() {
        Task task = new Task("Text");
        actionRepository.save(task);

        assertThat(actionRepository.getAll(), contains(task));
    }


}
