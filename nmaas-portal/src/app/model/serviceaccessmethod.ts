export enum ServiceAccessMethodType {
    DEFAULT,
    EXTERNAL,
    INTERNAL
}

export class ServiceAccessMethod {
    public type: ServiceAccessMethodType = undefined;
    public name : string = undefined;
    public url : string = undefined;
}
