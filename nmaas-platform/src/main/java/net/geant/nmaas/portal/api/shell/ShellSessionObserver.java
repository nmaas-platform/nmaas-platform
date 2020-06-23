package net.geant.nmaas.portal.api.shell;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * this class is responsible for sending events to event stream from given observable
 * it OBSERVES event source (e.g. ssh session) and emits events to event emitter
 */
@Getter
@Log4j2
public class ShellSessionObserver implements Observer {

    private static final Long DEFAULT_HEARTBEAT_INTERVAL_MS = 30000L;
    private static final Long SSE_TIMEOUT_24H_MS = 86400000L;

    // TODO verify if emitter access should be synchronized
    private final SseEmitter emitter;
    private final ExecutorService executor;

    // send very long timeout
    // 24 hours
    public ShellSessionObserver() {
        this.emitter = new SseEmitter(SSE_TIMEOUT_24H_MS);

        // send heartbeat to assure that connection is not closed on client side
        this.executor = Executors.newSingleThreadExecutor();
        this.executor.execute(() -> {
            try {
                for(long i = 0L; i < SSE_TIMEOUT_24H_MS/DEFAULT_HEARTBEAT_INTERVAL_MS; i++) {
                    SseEmitter.SseEventBuilder builder = SseEmitter.event()
                            .name("heartbeat")
                            .id(Long.toString(i))
                            .comment("heartbeat");

                    this.emitter.send(builder);

                    Thread.sleep(DEFAULT_HEARTBEAT_INTERVAL_MS);
                }
            } catch (IOException e) {
                this.emitter.completeWithError(e);
                log.error("Failed to send heartbeat");
                log.error(e.getMessage());
            } catch (InterruptedException e) {
                log.warn("Heartbeat thread was interrupted");
                log.warn(e.getMessage());
            }
        });
    }

    @Override
    public void update(Observable observable, Object o) {
        try {
            this.emitter.send(o);
            log.debug("Message:\t" + o.toString());
        } catch (IOException e) {
            this.emitter.completeWithError(e);
            log.error("Failed to send message:\t" + o.toString());
            log.error(e.getMessage());
        }
    }

    /**
     * close the sse connection
     */
    public void complete() {
        this.executor.shutdownNow();
        this.emitter.complete();
    }

}
