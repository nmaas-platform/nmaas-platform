package net.geant.nmaas.orchestration.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * User access details to the deployed application, typically its graphical user interface. In case of
 * web based GUIs this would be a HTTP URL.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AppUiAccessDetails {

    /**
     * Simple HTTP URL to access the deployed application UI
     */
    private String url;
}
