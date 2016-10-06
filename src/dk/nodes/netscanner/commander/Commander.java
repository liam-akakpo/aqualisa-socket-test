package dk.nodes.netscanner.commander;

import java.lang.reflect.Field;

/**
 * Created by liamakakpo on 26/09/2016.
 */
public class Commander<T> {

    private Object target;
    private boolean logOutput = false;

    public Commander(Object target) {
        this.target = target;
    }

    public interface Task<T> {
        void task(T object);
    }

    public Commander<T> log(final boolean logOutput) {
        this.logOutput = logOutput;
        return this;
    }

    public Executable<T> perform(final Task<T> task) {
        return new Executable<>(target, task);
    }

    public class Executable<T> {

        private Task<T> task;
        private Object target;

        public Executable(Object target, Task<T> task) {
            this.target = target;
            this.task = task;
        }

        public void onAll(final Class c) {
            Field[] fields = target.getClass().getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                if (c.isAssignableFrom(f.getType())) {
                    try {
                        task.task((T) f.get(target));
                        if (logOutput) {
                            System.out.println("Task executed on '" + f.getName() + "'");
                        }
                    } catch (IllegalAccessException e) {

                    }
                }
            }
        }

        public void on(T... t) {
            for (T target : t) {
                task.task(target);
                if (logOutput) {
                    System.out.println("Task executed on '" + t.toString() + "'");
                }
            }
        }
    }
}
