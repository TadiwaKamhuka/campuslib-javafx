package com.team.campuslib.observer;

import javafx.application.Platform;

public final class FxDataChangeListener implements DataChangeListener {

    private final DataChangeType filter;
    private final Runnable action;

    public FxDataChangeListener(DataChangeType filter, Runnable action) {
        this.filter = filter;
        this.action = action;
    }

    @Override
    public void onDataChanged(DataChangeType type) {
        if (filter != null && filter != type) {
            return;
        }
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
