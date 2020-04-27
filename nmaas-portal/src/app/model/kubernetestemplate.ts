import {KubernetesChart} from "./kuberneteschart";

export class KubernetesTemplate{
    public chart: KubernetesChart = new KubernetesChart();
    public archive: string = undefined;
    public mainDeploymentName: string = undefined;
}