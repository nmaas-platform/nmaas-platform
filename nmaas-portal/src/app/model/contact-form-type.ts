export enum AccessModifier {
    ALL= 'ALL',
    ONLY_LOGGED_IN = 'ONLY_LOGGED_IN',
    ONLY_NOT_LOGGED_IN = 'ONLY_NOT_LOGGED_IN'
}

export function parseAccessModifier(value: string|AccessModifier): AccessModifier {
    if (typeof value === 'string') {
        return AccessModifier[value];
    }
    return value;
}

export interface ContactFormType {
    key: string, // contact form subType
    access: AccessModifier, // access modifier
    templateName: string, // name of the template to download
}
