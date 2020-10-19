export enum AccessModifier {
    ALL,
    ONLY_LOGGED_IN,
    ONLY_NOT_LOGGED_IN
}

export interface ContactFormType {
    key: string,
    emailSubject?: string,
    access: AccessModifier,
    templateName: string,
    template?: any,
    dropdownText?: string
}
