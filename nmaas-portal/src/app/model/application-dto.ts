import {ApplicationBase} from './application-base';
import {Application} from './application';

export class ApplicationDTO {
    public applicationBase: ApplicationBase = undefined;
    public application: Application = undefined;

    constructor() {
        this.applicationBase = new ApplicationBase();
        this.application = new Application()
    }
}
