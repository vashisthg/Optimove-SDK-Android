package com.optimove.sdk.optimove_sdk.main.event_handlers;

import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.OptimoveCoreEvent;
import com.optimove.sdk.optimove_sdk.main.events.decorators.OptimoveCustomEventDecorator;

import java.util.ArrayList;
import java.util.List;

public class EventNormalizer extends EventHandler {


    @Override
    public void reportEvent(List<OptimoveEvent> optimoveEvents) {
        List<OptimoveEvent> optimoveEventsNormalized = new ArrayList<>();

        for (OptimoveEvent optimoveEvent: optimoveEvents) {
            if (!(optimoveEvent instanceof OptimoveCoreEvent)) {
                optimoveEventsNormalized.add(new OptimoveCustomEventDecorator(optimoveEvent));
            }  else {
                optimoveEventsNormalized.add(optimoveEvent);
            }
        }
        reportEventNext(optimoveEventsNormalized);
    }
}
