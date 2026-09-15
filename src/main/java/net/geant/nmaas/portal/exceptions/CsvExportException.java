package net.geant.nmaas.portal.exceptions;

public class CsvExportException extends RuntimeException {

    public CsvExportException(String message) {
        super(message);
    }

    public CsvExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
