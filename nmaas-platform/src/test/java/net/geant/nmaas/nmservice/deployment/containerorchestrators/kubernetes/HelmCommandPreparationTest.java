package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class HelmCommandPreparationTest {

    private static final String NAMESPACE = "nmaas";
    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("deploymentId");
    private static final String CHART_ARCHIVE_NAME = "/home/nmaas/charts/testapp-0.0.1";
    private static final String CORRECT_HELM_INSTALL_COMMAND =
            "helm install --name " + DEPLOYMENT_ID.value() + " --namespace " + NAMESPACE + " " + CHART_ARCHIVE_NAME;
    private static final String CORRECT_HELM_DELETE_COMMAND = "helm delete " + DEPLOYMENT_ID.value();
    private static final String CORRECT_HELM_STATUS_COMMAND = "helm status " + DEPLOYMENT_ID.value();

    @Test
    public void shouldConstructInstallCommandWithNoArguments() {
        assertThat(HelmInstallCommand.command(NAMESPACE, DEPLOYMENT_ID.value(), null, CHART_ARCHIVE_NAME).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND));
    }

    @Test
    public void shouldConstructDeleteCommand() {
        assertThat(HelmDeleteCommand.command(DEPLOYMENT_ID.value()).asString(), equalTo(CORRECT_HELM_DELETE_COMMAND));
    }

    @Test
    public void shouldConstructStatusCommand() {
        assertThat(HelmStatusCommand.command(DEPLOYMENT_ID.value()).asString(), equalTo(CORRECT_HELM_STATUS_COMMAND));
    }

}
