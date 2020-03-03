import {ServiceAccessMethodType} from "./serviceaccessmethod";

export class AppAccessMethod {
    public type: ServiceAccessMethodType;
    public name: string;
    public tag: string;
    public deployParameters: Map<string, string> = new Map();
}
