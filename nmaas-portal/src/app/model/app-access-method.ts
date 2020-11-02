import {ServiceAccessMethodType} from './service-access-method';

export class AppAccessMethod {
    public id: number;
    public type: ServiceAccessMethodType;
    public name: string;
    public tag: string;
    public deployParameters: object = {}; // this should be Map<string, string> but JS cannot stringify object of this type
}
