package net.fibulwinter.gtd.domain;

import net.fibulwinter.gtd.service.TaskListService;

public class Context {

    public static final Context DEFAULT = new Context("@Anywhere");
    public static final Context ANY = new Context("<Any>", true) {
        @Override
        public boolean match(Task task) {
            return true;
        }
    };
    public static final Context TODAY = new Context("<Today>", true) {
        @Override
        public boolean match(Task task) {
            return TaskListService.TODAY_PREDICATE().apply(task);
        }
    };
    public static final Context OVERDUE = new Context("<Overdue>", true) {
        @Override
        public boolean match(Task task) {
            return TaskListService.OVERDUE_PREDICATE().apply(task);
        }
    };

    private final String name;
    private final boolean special;

    public Context(String name) {
        this(name, false);
    }

    private Context(String name, boolean special) {
        this.name = name;
        this.special = special;
    }

    public String getName() {
        return name;
    }

    public boolean isSpecial() {
        return special;
    }

    public boolean isDefault() {
        return Context.DEFAULT.equals(this);
    }

    public boolean match(Task task) {
        return this.equals(task.getContext());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Context context = (Context) o;

        if (special != context.special) return false;
        if (!name.equals(context.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (special ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
