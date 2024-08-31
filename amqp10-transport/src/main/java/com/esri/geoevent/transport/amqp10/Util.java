package com.esri.geoevent.transport.amqp10;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Util {

    private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(AMQP10InboundTransport.class);

    static void shutdownExecutorService(ExecutorService service, int timeout) {
        service.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!service.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                service.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!service.awaitTermination(timeout, TimeUnit.MILLISECONDS))
                    LOGGER.warn("EXECUTOR_SERVICE_TERMINATION_TIMEOUT", timeout);
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            service.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
