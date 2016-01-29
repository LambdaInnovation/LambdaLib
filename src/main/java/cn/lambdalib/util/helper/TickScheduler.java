/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.helper;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Util to aid timed tick scheduling. You can use it at anywhere that requires ticking to handle multiple schedules.
 */
public class TickScheduler {

    private class Schedule {

        Optional<Supplier<Boolean>> condition;
        Optional<String> name;
        int interval;
        Runnable runnable;

        int counter;
        boolean disposed;

    }

    private List<Schedule> schedules = new LinkedList<>();

    public class ScheduleCreator {

        Optional<Supplier<Boolean>> condition = Optional.empty();
        Optional<String> name = Optional.empty();

        Side runSide = null;

        int tickIntv;

        public ScheduleCreator(int _interval) {
            tickIntv = _interval;
        }

        public ScheduleCreator atOnly(Side side) {
            runSide = side;
            return this;
        }

        public ScheduleCreator condition(Supplier<Boolean> _condition) {
            if (shouldIgnore())
                return this;

            condition = Optional.of(_condition);
            return this;
        }

        public ScheduleCreator name(String _name) {
            if (shouldIgnore())
                return this;

            check(!name.isPresent(), "Name must be previously empty");
            name = Optional.of(_name);
            return this;
        }

        public ScheduleCreator run(Runnable _task) {
            if (shouldIgnore())
                return this;

            check(name.equals(Optional.empty()) ||
                    !schedules.stream().anyMatch(s -> s.name.equals(name)), "Name collide: " + name);
            Schedule add = new Schedule();
            add.name = name;
            add.condition = condition;
            add.interval = tickIntv;
            add.runnable = _task;
            add.counter = tickIntv;
            schedules.add(add);
            return this;
        }

        private boolean shouldIgnore() {
            return runSide != null && runSide != FMLCommonHandler.instance().getEffectiveSide();
        }

    }

    public ScheduleCreator everyTick() {
        return every(1);
    }

    public ScheduleCreator every(int ticks) {
        return new ScheduleCreator(ticks);
    }

    public void remove(String name) {
        Optional<String> cmp = Optional.of(name);
        schedules.stream()
                .filter(s -> s.name.equals(cmp))
                .forEach(s -> s.disposed = true);
    }

    public void updateInterval(String name, int newInterval) {
        find(name).ifPresent(s -> {
            s.interval = newInterval;
        });
    }

    public void runTick() {
        Iterator<Schedule> itr = schedules.iterator();
        while (itr.hasNext()) {
            Schedule s = itr.next();
            if (s.disposed) {
                itr.remove();
            } else {
                if (!s.condition.isPresent() || s.condition.get().get()) {
                    if (--s.counter <= 0) {
                        s.runnable.run();
                        s.counter = s.interval;
                    }
                }
            }
        }
    }

    private void check(boolean pred, String msg) {
        if (!pred) throw new RuntimeException("TickScheduler: " + msg);
    }

    private Optional<Schedule> find(String name) {
        Optional<String> cmp = Optional.of(name);
        return schedules.stream().filter(s -> s.name.equals(cmp)).findFirst();
    }

}
