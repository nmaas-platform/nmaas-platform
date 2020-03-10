import {AppInstance} from "./appinstance";
import {Application} from "./application";
import {Domain} from "./domain";

export class AppInstanceExtended extends AppInstance {
    public application: Application;
    public domain: Domain;
}
