package net.fibulwinter.gtd.presentation;

import java.util.Comparator;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.domain.Task;

public interface SortAndGroup extends Comparator<Task>, Function<Task, Optional<?>> {
    int getResource();

}
