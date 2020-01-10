package net.geant.nmaas.orchestration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;

import java.util.Set;

/**
 * User access details to the deployed application, typically its graphical user interface. In case of
 * web based GUIs this would be a HTTP URL.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AppUiAccessDetails {

    /**
     * set of access methods to deployed application UI
     */
    private Set<ServiceAccessMethod> serviceAccessMethods;

}
