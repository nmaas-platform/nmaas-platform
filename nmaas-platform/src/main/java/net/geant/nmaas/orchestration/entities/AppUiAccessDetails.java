package net.geant.nmaas.orchestration.entities;

/**
 * User access details to the deployed application, typically its graphical user interface. In case of
 * web based GUIs this would be a HTTP URL.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AppUiAccessDetails {

    /**
     * Simple HTTP URL to access the deployed application UI
     */
    private String url;

    public AppUiAccessDetails() {}

    public AppUiAccessDetails(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
