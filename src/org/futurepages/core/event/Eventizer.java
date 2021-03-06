package org.futurepages.core.event;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import org.futurepages.apps.simple.SimpleUI;
import org.futurepages.core.exception.AppLogger;

/**
 * A simple wrapper for Guava event bus. Defines static convenience methods for
 * relevant actions.
 */
public class Eventizer implements SubscriberExceptionHandler {

    private final EventBus eventBus = new EventBus(this);

    public static void post(final Object event) {
        currentEventBus().post(event);
    }

    public static void register(final Object object) {
        currentEventBus().register(object);
    }

    public static void unregister(final Object object) {
        currentEventBus().unregister(object);
    }


    private static EventBus currentEventBus() {
        return SimpleUI.getEventizer().eventBus;
    }

    @Override
    public final void handleException(final Throwable exception, final SubscriberExceptionContext context) {
        //TODO use context too.
        //TODO understand it.
        AppLogger.getInstance().execute(exception);
    }
}
