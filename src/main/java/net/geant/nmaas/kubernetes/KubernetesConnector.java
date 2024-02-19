package net.geant.nmaas.kubernetes;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import lombok.extern.log4j.Log4j2;
import okhttp3.Response;
import org.apache.commons.lang3.NotImplementedException;

import java.io.InputStream;

@Log4j2
public abstract class KubernetesConnector implements AsyncConnector {

    public static class SimpleListener implements ExecListener {
        @Override
        public void onOpen(Response response) {
            log.info("Shell opened");
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            log.info("Shell connection broke");
        }

        @Override
        public void onClose(int code, String reason) {
            log.info("Shell connection will now close");
        }
    }

    protected String podName;
    protected String namespace;

    protected transient Config config;
    protected transient KubernetesClient client;

    @Override
    public void executeCommand(String command) {
        throw new NotImplementedException();
    }

    @Override
    public InputStream getErrorStream() {
        throw new NotImplementedException();
    }

    @Override
    public void close() {
        throw new NotImplementedException();

    }

    @Override
    public String executeSingleCommand(String command) {
        throw new NotImplementedException();
    }

}
