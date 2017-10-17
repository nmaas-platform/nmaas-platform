import { User } from './user';
import { AppInstanceState } from './appinstancestatus';

export class AppInstance {
    id: Number;
    applicationId: Number;
    applicationName: string;
    name: string;
    createdAt: Date;
    owner: User;
    configuration: string;
    state: AppInstanceState;
    url: string;
}