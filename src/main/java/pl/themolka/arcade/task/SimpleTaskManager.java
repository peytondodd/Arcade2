package pl.themolka.arcade.task;

import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.util.Tickable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class SimpleTaskManager implements TaskManager, Tickable {
    private final ArcadePlugin plugin;

    private int lastUniqueId = 0;
    private final List<TaskExecutor> taskList = new CopyOnWriteArrayList<>();

    public SimpleTaskManager(ArcadePlugin plugin) {
        this.plugin = plugin;
    }

    // Runnable
    @Override
    public void onTick(long tick) {
        for (TaskExecutor task : this.getTasks()) {
            try {
                task.run();
            } catch (Throwable th) {
                this.plugin.getLogger().log(Level.SEVERE, "Could not run task executor #" + task.getId(), th);
            }
        }
    }

    @Override
    public boolean cancel(TaskExecutor task) {
        if (task == null) {
            return false;
        }

        task.destroyTask();
        return this.taskList.remove(task);
    }

    @Override
    public List<TaskExecutor> getTasks() {
        return this.taskList;
    }

    @Override
    public int schedule(TaskExecutor executor) {
        executor.createTask();
        this.taskList.add(executor);

        return executor.getId();
    }

    @Override
    public int scheduleAsync(TaskListener task) {
        return this.schedule(new AsyncTaskExecutor(this.plugin, this.getNextUniqueId(), task));
    }

    @Override
    public int scheduleSync(TaskListener task) {
        return this.schedule(new TaskExecutor(this.plugin, this.getNextUniqueId(), task));
    }

    private synchronized int getNextUniqueId() {
        return ++this.lastUniqueId;
    }

    private synchronized int generateNextUniqueId() {
        int result = 0;
        while (this.getTask(result) != null) {
            result++;
        }

        return result + 1;
    }
}