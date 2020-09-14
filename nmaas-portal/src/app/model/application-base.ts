import {AppDescription} from './appdescription';
import {ApplicationVersion} from './applicationversion';
import {Rate} from './rate';

export class ApplicationBase {
    public id: number = undefined;
    public name: string = undefined;

    public license: string = undefined;
    public licenseUrl: string = undefined;

    public wwwUrl: string = undefined;
    public sourceUrl: string = undefined;
    public issuesUrl: string = undefined;
    public nmaasDocumentationUrl: string = undefined;

    public descriptions: AppDescription[] = [];
    public tags: string[] = [];
    public versions: ApplicationVersion[] = [];

    public rate: Rate = undefined;
}
