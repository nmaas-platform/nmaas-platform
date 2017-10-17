package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class HelmInstallCommandExecutionTest {

    private static final String NAMESPACE = "nmaas";
    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("deploymentId");
    private static final String CHART_ARCHIVE_NAME = "/home/nmaas/charts/testapp-0.0.1";
    private static final String CORRECT_HELM_INSTALL_COMMAND =
            "helm install --name " + DEPLOYMENT_ID.value() + " --namespace " + NAMESPACE + " " + CHART_ARCHIVE_NAME;

    @Test
    public void shouldConstructInstallCommandWithNoArguments() {
        assertThat(HelmInstallCommand.command(NAMESPACE, DEPLOYMENT_ID.value(), null, CHART_ARCHIVE_NAME).asString(),
                equalTo(CORRECT_HELM_INSTALL_COMMAND));
    }

}
