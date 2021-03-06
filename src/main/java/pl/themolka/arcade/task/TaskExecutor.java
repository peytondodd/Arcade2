/*
 * Copyright 2018 Aleksander Jagiełło
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.themolka.arcade.task;

import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.time.Time;
import pl.themolka.arcade.util.IncrementalId;

public class TaskExecutor implements Comparable<TaskExecutor>, IncrementalId,  Runnable {
    public static final long TICK_SLEEP = Time.SECOND.toTicks();

    private final ArcadePlugin plugin;

    private final int id;
    private final TaskListener listener;
    private long ticks, seconds, minutes, hours, days;

    public TaskExecutor(ArcadePlugin plugin, int id, TaskListener listener) {
        this.plugin = plugin;

        this.id = id;
        this.listener = listener;
    }

    @Override
    public int compareTo(TaskExecutor object) {
        return Integer.compare(this.getId(), object.getId());
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public void run() {
        if (this.getId() == Task.DEFAULT_TASK_ID) {
            // not running
            return;
        }

        // ticks
        this.getListener().onTick(this.ticks++);

        // seconds
        if (this.ticks % TICK_SLEEP == 0) {
            this.getListener().onSecond(this.seconds++);

            // minutes
            if (this.seconds % 60 == 0) {
                this.getListener().onMinute(this.minutes++);
            
                // hours
                if (this.minutes % 60 == 0) {
                    this.getListener().onHour(this.hours++);

                    // days
                    if (this.hours % 24 == 0) {
                        this.getListener().onDay(this.days++);
                    }
                }
            }
        }
    }

    public void createTask() {
        this.getListener().onCreate();
    }

    public void destroyTask() {
        this.getListener().onDestroy();
    }

    public TaskListener getListener() {
        return this.listener;
    }

    public boolean isAsync() {
        return false;
    }
}
