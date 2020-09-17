export enum ApplicationState {
    NEW,
    ACTIVE,
    REJECTED,
    DISABLED,
    DELETED
}

export function parseApplicationState(state: string | ApplicationState): ApplicationState {
    switch (typeof state) {
        case 'string':
            return ApplicationState[state];
        default:
            return state;
    }
}
