package pl.themolka.arcade.task;

public class Task extends SimpleTaskListener {
    public static final int DEFAULT_TASK_ID = -1;

    private int taskId = DEFAULT_TASK_ID;
    private final TaskManager tasks;

    public Task(TaskManager tasks) {
        this.tasks = tasks;
    }

    public boolean cancelTask() {
        boolean result = this.tasks.cancel(this);
        this.setTaskId(DEFAULT_TASK_ID);

        return result;
    }

    public int getTaskId() {
        return this.taskId;
    }

    public TaskManager getTasks() {
        return this.tasks;
    }

    public boolean isTaskRunning() {
        return this.taskId != DEFAULT_TASK_ID;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Task scheduleAsyncTask() {
        this.taskId = this.tasks.scheduleAsync(this);
        return this;
    }

    public Task scheduleSyncTask() {
        this.taskId = this.tasks.scheduleSync(this);
        return this;
    }
}
