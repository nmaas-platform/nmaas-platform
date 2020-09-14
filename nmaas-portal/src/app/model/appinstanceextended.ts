import {AppInstance} from './appinstance';
import {ApplicationMassive} from './application-massive';
import {Domain} from './domain';

export class AppInstanceExtended extends AppInstance {
    public application: ApplicationMassive;
    public domain: Domain;
}
