package com.cefalo.cci.event.listener;

import com.cefalo.cci.event.model.Event;

public interface EventListener {
    void handleEvent(final Event event);
}
