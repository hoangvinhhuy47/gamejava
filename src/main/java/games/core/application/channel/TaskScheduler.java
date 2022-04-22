package games.core.application.channel;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class TaskScheduler {
    private static TaskScheduler instance = new TaskScheduler();

    public static TaskScheduler getInstance() {
        if (instance == null) {
            instance = new TaskScheduler();
        }
        return instance;
    }
    private static final int SIZE = 5;

    private ScheduledThreadPoolExecutor taskScheduler;

    public TaskScheduler() {
        init();
    }

    public int getThreadPoolSize() {
        return taskScheduler.getCorePoolSize();
    }

    public void resizeThreadPool(int threadPoolSize) {
        taskScheduler.setCorePoolSize(threadPoolSize);
    }

    public ScheduledFuture<?> schedule(Runnable task, int delay, TimeUnit unit) {
        return taskScheduler.schedule(task, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, int initialDelay, int period, TimeUnit unit) {
        return taskScheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public void destroy(Object o) {
        List<Runnable> awaitingExecution = taskScheduler.shutdownNow();
        //logger.info("TaskScheduler stopping. Tasks awaiting execution: " + awaitingExecution.size());
    }

    public void init() {
        if (taskScheduler == null) {
            taskScheduler = new ScheduledThreadPoolExecutor(SIZE);
        }
    }
    public void shutDown(){
        taskScheduler.shutdown();
    }
}
