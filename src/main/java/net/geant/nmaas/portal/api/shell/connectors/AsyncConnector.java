package net.geant.nmaas.portal.api.shell.connectors;

import java.io.InputStream;
import java.io.Serializable;

public interface AsyncConnector extends Serializable {

    void executeCommand(String command);
    String executeSingleCommand(String command);
    InputStream getInputStream();
    InputStream getErrorStream();
    void close();

}
