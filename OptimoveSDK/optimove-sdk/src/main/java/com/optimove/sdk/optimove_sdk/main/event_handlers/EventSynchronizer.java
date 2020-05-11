package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class EventSynchronizer extends EventHandler {

    private ExecutorService singleThreadExecutor;

    public EventSynchronizer(ExecutorService executorService) {
        this.singleThreadExecutor = executorService;
    }

    @Override
    public void reportEvent(List<OptimoveEvent> optimoveEvents) {
        singleThreadExecutor.submit(() ->
            this.reportEventNext(optimoveEvents));
    }

}
