import {ApplicationState} from './application-state';

export class ApplicationVersion {
    public id: number;
    public version: string;
    public state: ApplicationState;
    public appVersionId: number;
}
