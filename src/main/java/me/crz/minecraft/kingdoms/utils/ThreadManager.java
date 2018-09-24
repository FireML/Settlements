package me.crz.minecraft.kingdoms.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

public class ThreadManager
{
    private static ThreadManager instance;
    private final ThreadGroup threads;
    private final Executor pool;
    
    public ThreadManager() {
        super();
        threads = new ThreadGroup("Kingdoms Thread Pool");
        final Thread thread;
        pool = Executors.newCachedThreadPool(r -> {
            new Thread(threads, r, String.valueOf(threads.getName()) + threads.activeCount());
            return thread;
        });
    }
    
    public void execute(final KingdomRunnable kr, final int seconds) {
        pool.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(seconds);
                kr.run();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void execute(final KingdomRunnable kr) {
        pool.execute(() -> kr.run());
    }
    
    public static ThreadManager getInstance() {
        if (instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }
    
    private /* synthetic */ Thread lambda$0(final Runnable r) {
        return new Thread(threads, r, String.valueOf(threads.getName()) + threads.activeCount());
    }
    
    private static /* synthetic */ void lambda$1(final int n, final KingdomRunnable kingdomRunnable) {
        try {
            TimeUnit.SECONDS.sleep(n);
            kingdomRunnable.run();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private static /* synthetic */ void lambda$2(final KingdomRunnable kingdomRunnable) {
        kingdomRunnable.run();
    }
}
