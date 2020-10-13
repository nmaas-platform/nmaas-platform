import {AppInstance} from './app-instance';
import {Domain} from './domain';
import {ApplicationDTO} from './application-dto';

export class AppInstanceExtended extends AppInstance {
    public application: ApplicationDTO;
    public domain: Domain;
}
