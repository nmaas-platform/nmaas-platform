package net.geant.nmaas.portal.api.shell;

import lombok.Getter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * this class is responsible for sending events to event stream from given observable
 */
@Getter
public class ShellSessionObserver implements Observer {

    private final SseEmitter emitter;

    public ShellSessionObserver() {
        this.emitter = new SseEmitter();
    }

    @Override
    public void update(Observable observable, Object o) {
        try {
            this.emitter.send(o);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
