package net.geant.nmaas.portal.api.shell.connectors;

import java.io.InputStream;

public interface AsyncConnector {

    void executeCommand(String command);
    String executeSingleCommand(String command);
    InputStream getInputStream();
    InputStream getErrorStream();
    void close();

}
