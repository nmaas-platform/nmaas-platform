package net.geant.nmaas.portal.api.shell.observable;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.shell.ShellCommandRequest;
import net.geant.nmaas.portal.api.shell.connectors.AsyncConnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * this class is responsible for maintaining ssh connection and command execution logic
 * currently does nothing but echo
 */
@Log4j2
@NoArgsConstructor
public class SshConnectionShellSessionObservable extends GenericShellSessionObservable {

    private String sessionId;
    private AsyncConnector sshConnector;
    private ExecutorService resultReader;
    private ExecutorService errorReader;

    /**
     * Purpose of this class is to read results from shell input stream
     *
     */
    private static class ShellResultReader {

        private final Reader reader;

        public ShellResultReader(Reader reader) {
            this.reader = reader;
        }

        /**
         * Reads single message from the shell, it can be an entire line of result,
         * or part of line till command prompt is reached; e.g. `host@localhost:~/ $`
         * @return single message to be passed
         * @throws IOException exception
         */
        public String readWord() throws IOException {
            StringBuilder result = new StringBuilder();

            int ch = reader.read();
            while (ch != -1) {
                // skip endline characters
                if((char) ch == '\n') {
                    ch = reader.read(); // assure progress
                    continue;
                }
                if((char) ch == '\r') { // carriage return is replaced with newline token
                    result.append("<#>NEWLINE<#>"); // newline control token
                    break; // newline is the end of the message
                }
                result.append((char) ch);
                if(result.toString().endsWith("$ ")) { // break after reaching command prompt
                    break; // reaching prompt is the end of the message
                }
                if(result.toString().endsWith("# ")) { // break after reaching command prompt
                    break; // reaching prompt is the end of the message
                }
                ch = reader.read(); // finally read next character
            }
            if(ch == -1) {
                return null;
            }

            return result.toString();
        }
    }

    /**
     * Creates observable, sets up SSH connection
     * @param sessionId
     * @param connector
     */
    public SshConnectionShellSessionObservable(String sessionId, AsyncConnector connector) {
        this.sessionId = sessionId;
        this.sshConnector = connector;

        /*
          below executors are used to read results from input stream asynchronously
          this solution may prove to be inefficient
          it is suggested to use some kind of reactive library to handle this functionality e.g. JavaRX
         */
        resultReader = Executors.newSingleThreadExecutor();
        resultReader.execute(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.sshConnector.getInputStream()));
            ShellResultReader shellResultReader = new ShellResultReader(reader);
            try {
                String part = shellResultReader.readWord();
                while (part != null) {
                    log.debug("Part:\t" + part);
                    this.sendMessage(part);
                    part = shellResultReader.readWord();
                }
                log.info("Result Line reader finished");
            } catch (IOException e) {
                log.error(e.getMessage());
            } finally {
                log.info("Result reader closed");
            }
        });

        errorReader = Executors.newSingleThreadExecutor();
        errorReader.execute(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.sshConnector.getErrorStream()));
            try {
                String line = reader.readLine();
                while (line != null) {
                    log.debug("Error:\t" + line);
                    this.sendMessage(line);
                    line = reader.readLine();
                }
                log.info("Error Line reader finished");
            } catch (IOException e) {
                log.error(e.getMessage());
            } finally {
                log.info("Error reader closed");
            }
        });

    }

    /**
     * Executes command synchronously returning result immediately,
     * However command is executed in single session scope (one command <=> one session)
     * Execution results are automatically sent to observers
     * This method has only debug purpose
     * @param commandRequest command request
     */
    public void executeCommand(ShellCommandRequest commandRequest) {
        log.info(sessionId + "\tCOMMAND:\t" + commandRequest.getCommand());

        String result = this.sshConnector.executeSingleCommand(commandRequest.getCommand());

        log.info(sessionId + "\tRESULT:\t" + result);
        for(String r: result.split("\n")) {
            this.sendMessage(r);
        }
    }

    /**
     * Executes single command in asynchronous manner
     * Results of the command are read from input stream by executors and submitted to observers
     * @param commandRequest command request
     */
    public void executeCommandAsync(ShellCommandRequest commandRequest) {
        log.debug(sessionId + "\tCOMMAND:\t" + commandRequest.getCommand());

        this.sshConnector.executeCommand(commandRequest.getCommand());
    }

    /**
     * completes the observable, destroys connection and readers
     */
    public void complete() {
        this.sshConnector.close();
        this.resultReader.shutdownNow();
        this.errorReader.shutdownNow();
        super.complete();
    }

    private synchronized void sendMessage(String message) {
        this.setChanged();
        this.notifyObservers(message);
    }
}
