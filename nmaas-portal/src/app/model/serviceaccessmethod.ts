export enum ServiceAccessMethodType {
    DEFAULT= 'DEFAULT',
    EXTERNAL = 'EXTERNAL',
    INTERNAL = 'INTERNAL',
    PUBLIC = 'PUBLIC'
}

export class ServiceAccessMethod {
    public type: ServiceAccessMethodType = undefined;
    public name: string = undefined;
    public protocol: string = undefined;
    public url: string = undefined;

    public static getServiceAccessMethodTypeAsEnum(arg: string | ServiceAccessMethodType): ServiceAccessMethodType {
        if (typeof arg === 'string') {
            return ServiceAccessMethodType[arg];
        }
        return arg;
    }

}
