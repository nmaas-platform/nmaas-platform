import { ConfigTemplate } from './configtemplate';

export class Application {

    constructor(public id: Number,
        public name: string,
        public version: string,
        public license: string,
    	public wwwUrl: string,
	    public sourceUrl: string,
	    public issuesUrl: string,
        public briefDescription: string,
        public fullDescription: string,
        public tags:string[],
        public configTemplate: ConfigTemplate) {

    }
}