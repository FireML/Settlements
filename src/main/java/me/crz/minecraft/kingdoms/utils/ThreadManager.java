package me.crz.minecraft.kingdoms.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

public class ThreadManager {
    private static ThreadManager instance;
    private final ThreadGroup threads;
    private final Executor pool;

    public ThreadManager() {
        super();
        threads = new ThreadGroup("Kingdoms Thread Pool");
        pool = Executors.newCachedThreadPool(r -> new Thread(threads, r, String.valueOf(threads.getName()) + threads.activeCount()));
    }

    public void execute(final KingdomRunnable kr, final int seconds) {
        pool.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(seconds);
                kr.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void execute(final KingdomRunnable kr) {
        pool.execute(kr::run);
    }

    synchronized public static ThreadManager getInstance() {
        if (instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }
}
