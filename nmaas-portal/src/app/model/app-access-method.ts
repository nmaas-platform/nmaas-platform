import {ServiceAccessMethod, ServiceAccessMethodType} from "./serviceaccessmethod";

export class AppAccessMethod {
    public type: ServiceAccessMethodType;
    public name: string;
    public tag: string;
    public deployParameters: object = {}; // this should be Map<string, string> but JS cannot stringify object of this type
}
