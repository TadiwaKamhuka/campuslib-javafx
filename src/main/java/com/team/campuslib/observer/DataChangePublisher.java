package com.team.campuslib.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DataChangePublisher {

    private static final DataChangePublisher INSTANCE =
            new DataChangePublisher();

    private final List<DataChangeListener> listeners =
            new CopyOnWriteArrayList<>();

    private DataChangePublisher() {
    }

    public static DataChangePublisher getInstance() {
        return INSTANCE;
    }

    public void subscribe(DataChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(DataChangeListener listener) {
        listeners.remove(listener);
    }

    public static void publish(DataChangeType type) {
        for (DataChangeListener listener : INSTANCE.listeners) {
            try {
                listener.onDataChanged(type);
            } catch (RuntimeException e) {
                System.err.println("Listener failed: " + e.getMessage());
            }
        }
    }
}
