package net.fibulwinter.gtd.domain;

import com.google.common.base.Optional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class ContextRepository {
    private List<Context> contexts = newArrayList(
            Context.DEFAULT,
            new Context("@Work"),
            new Context("@Home"),
            new Context("@Errands"),
            new Context("@Waiting"),
            new Context("@Read")
    );
    private Map<String, Context> contextMap = newHashMap();

    {
        for (Context context : contexts) {
            contextMap.put(context.getName(), context);
        }
    }

    public List<Context> getAll() {
        return Collections.unmodifiableList(contexts);
    }

    public Optional<Context> getByName(String name) {
        return Optional.fromNullable(contextMap.get(name));
    }
}
