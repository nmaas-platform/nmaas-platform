import { User } from './user';
import { AppInstanceState } from './appinstancestatus';

export class AppInstance {
    id: Number;
    applicationId: Number;
    name: string;
    createdAt: Date;
    owner: User;  
    configuration: string;
    state: AppInstanceState;
}