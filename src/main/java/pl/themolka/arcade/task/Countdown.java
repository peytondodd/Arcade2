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

import pl.themolka.arcade.game.Game;
import pl.themolka.arcade.game.GameHolder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Countdown extends Task implements CountdownListener, GameHolder {
    private Duration duration;
    private boolean forcedCancel;
    private Game game;
    private long seconds;

    public Countdown(TaskManager tasks) {
        this(tasks, null);
    }

    public Countdown(TaskManager tasks, Duration duration) {
        super(tasks);

        if (duration != null) {
            this.setDuration(duration);
        }
    }

    @Override
    public Game getGame() {
        return this.game;
    }

    /**
     * Should not be used.
     * Use {@link #cancelCountdown()} instead.
     */
    @Deprecated
    @Override
    public final boolean cancelTask() {
        return super.cancelTask();
    }

    @Override
    public void onTick(long ticks) {
        super.onTick(ticks);
    }

    @Override
    public final void onSecond(long seconds) {
        super.onSecond(seconds);

        this.seconds++;
        this.onUpdate(this.seconds, this.getLeftSeconds());

        if (this.isDone()) {
            this.onDone();
            this.cancelCountdown();
        }
    }

    @Override
    public final void onMinute(long minutes) {
        super.onMinute(minutes);
    }

    @Override
    public final void onHour(long hours) {
        super.onHour(hours);
    }

    @Override
    public final void onDay(long days) {
        super.onDay(days);
    }

    /**
     * Should be @Override
     */
    public boolean isCancelable() {
        return true;
    }

    /**
     * Should be @Override
     */
    public void onCancel() {
    }

    /**
     * Should be @Override
     */
    @Override
    public void onDone() {
    }

    /**
     * Should be @Override
     */
    @Override
    public void onUpdate(long seconds, long secondsLeft) {
    }

    @Deprecated
    @Override
    public final Task scheduleAsyncTask() {
        throw new UnsupportedOperationException("Countdowns must be registered in the Game object.");
    }

    @Deprecated
    @Override
    public final Task scheduleSyncTask() {
        throw new UnsupportedOperationException("Countdowns must be registered in the Game object.");
    }

    public boolean cancelCountdown() {
        if (this.isCancelable()) {
            this.onCancel();
            this.cancelTask();
            this.seconds = 0;
            return true;
        }
        return false;
    }

    public int countAsync() {
        if (this.getGame() == null) {
            throw new UnsupportedOperationException("Game not registered");
        }

        return this.getGame().addAsyncTask(this);
    }

    public int countSync() {
        if (this.getGame() == null) {
            throw new UnsupportedOperationException("Game not registered");
        }

        return this.getGame().addSyncTask(this);
    }

    public Duration getDuration() {
        return this.duration;
    }

    public long getDurationSeconds() {
        if (this.getDuration() != null) {
            return this.getDuration().get(ChronoUnit.SECONDS);
        }

        return Long.MAX_VALUE;
    }

    public long getLeftSeconds() {
        return this.getDurationSeconds() - this.seconds;
    }

    public long getSeconds() {
        return this.seconds;
    }

    public boolean isDone() {
        return this.getLeftSeconds() <= 0L;
    }

    public boolean isForcedCancel() {
        return this.forcedCancel;
    }

    public Countdown setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public Countdown setForcedCancel(boolean forcedCancel) {
        this.forcedCancel = forcedCancel;
        return this;
    }

    public Countdown setGame(Game game) {
        this.game = game;
        return this;
    }
}
