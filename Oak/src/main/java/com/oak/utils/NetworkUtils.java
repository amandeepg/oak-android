/*
 * Copyright (c) 2013 Amandeep Grewal
 */

package com.oak.utils;

import java.util.Map;
import java.util.concurrent.Semaphore;

public class NetworkUtils {

    public static void incrementFire(Map<String, Semaphore> sems, String id) {
        Semaphore sem = sems.get(id);
        if (sem == null) {
            sem = new Semaphore(0);
            sems.put(id, sem);
        }
        sem.release();
    }

    public static void decrementFire(Map<String, Semaphore> sems, String id) {
        Semaphore sem = sems.get(id);
        sem.acquireUninterruptibly();
    }

    public static boolean areRequestsPending(Map<String, Semaphore> sems) {
        for (Semaphore sem: sems.values()) {
            if (sem.availablePermits() != 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldFireNow(Map<String, Semaphore> sems, String id) {
        Semaphore sem = sems.get(id);
        if (sem.availablePermits() == 1) {
            return true;
        } else {
            sem.acquireUninterruptibly();
            return false;
        }
    }
}
