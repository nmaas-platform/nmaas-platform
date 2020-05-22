package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class HelmCommandExecutorTest {

    private static final String EXAMPLE_HELM_STATUS_COMMAND_OUTPUT =
            "LAST DEPLOYED: Wed Dec  6 13:48:59 2017\n" +
            "NAMESPACE: default\n" +
            "STATUS: DEPLOYED\n" +
            "" +
            "RESOURCES:" +
            "==> v1/PersistentVolumeClaim" +
            "NAME                                  STATUS  VOLUME                                    CAPACITY  ACCESSMODES  STORAGECLASS         AGE" +
            "c21584cd-666c-42de-9df7-d72b7bae5aae  Bound   pvc-d4eb67d2-da83-11e7-bec9-5254002cd33f  1Gi       RWO          managed-nfs-storage  1m" +
            "" +
            "==> v1/Service" +
            "NAME                                                 CLUSTER-IP    EXTERNAL-IP  PORT(S)  AGE" +
            "c21584cd-666c-42de-9df7-d72b7bae5aae-nmaas-oxidized  10.13.82.246  <none>       80/TCP   1m" +
            "" +
            "==> v1beta1/Deployment" +
            "NAME                                                 DESIRED  CURRENT  UP-TO-DATE  AVAILABLE  AGE" +
            "c21584cd-666c-42de-9df7-d72b7bae5aae-nmaas-oxidized  1        1        1           1          1m" +
            "";

    @Test
    public void shouldReturnDeployedStatusFromInputString() {
        HelmCommandExecutor executor = new HelmCommandExecutor();
        assertThat(executor.parseStatus(EXAMPLE_HELM_STATUS_COMMAND_OUTPUT), equalTo(HelmPackageStatus.DEPLOYED));
    }

    @Test
    public void shouldReturnUnknownStatusFromInputString() {
        HelmCommandExecutor executor = new HelmCommandExecutor();
        assertThat(executor.parseStatus("this is some example string"), equalTo(HelmPackageStatus.UNKNOWN));
    }

    @Test
    public void shouldConstructChartName() {
        HelmCommandExecutor executor = new HelmCommandExecutor();
        executor.helmRepositoryName = "nmaas";
        assertThat(executor.constructChartNameWithRepo("chart-name"), equalTo("nmaas/chart-name"));
        assertThat(executor.constructChartNameWithRepo("repo-name/chart-name"), equalTo("repo-name/chart-name"));
    }

}
