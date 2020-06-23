import {KubernetesChart} from "./kuberneteschart";

export class KubernetesTemplate {
    public id: number;
    public chart: KubernetesChart = new KubernetesChart();
    public archive: string = undefined;
    public mainDeploymentName: string = undefined;
}
