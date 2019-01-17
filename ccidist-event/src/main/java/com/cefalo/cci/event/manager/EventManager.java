package com.cefalo.cci.event.manager;

import com.cefalo.cci.event.listener.EventListener;
import com.cefalo.cci.event.model.Event;
import com.google.inject.persist.UnitOfWork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

public class EventManager implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private UnitOfWork unitOfWork;

    private List<EventListener> eventListenerList = new CopyOnWriteArrayList<>();
    private BlockingQueue<Event> eventBlockingQueue = new ArrayBlockingQueue<>(1024);
    private AtomicBoolean stopRequested = new AtomicBoolean(false);

    private Thread thread;

    public void addListener(EventListener eventListener) {
        this.eventListenerList.add(eventListener);
    }

    public void doStart() {
        thread = new Thread(this, "EventManagerThread");
        thread.start();
    }

    public void doStop() {
        // FIXME: State management??? Consider using Guava Service class.
        if (thread != null) {
            stopRequested.set(true);
            thread.interrupt();
        }
    }

    public void post(Event event) {
        if (logger.isInfoEnabled()) {
            logger.info("New event: {}", event);
        }

        try {
            this.eventBlockingQueue.put(event);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            Event event = null;
            while ((event = eventBlockingQueue.take()) != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Recieved event: {}", event);
                }

                for (EventListener eventListener : eventListenerList) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Sending event: {} to event listener: {}", event, eventListener);
                    }
                    // Lets setup JPA here.
                    unitOfWork.begin();
                    try {
                        eventListener.handleEvent(event);
                    } catch (Exception ex) {
                        logger.error("{} incurred problem while processing event: {}", eventListener, event, ex);
                    } finally {
                        // Tear JPA down.
                        unitOfWork.end();
                    }
                }
            }
        } catch (InterruptedException e) {
            if (!stopRequested.get()) {
                logger.error("EventManager thread stopped unexpectedly. Error: ", e);
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("EventManager thread stopped.");
                }
            }
        }
    }
}
