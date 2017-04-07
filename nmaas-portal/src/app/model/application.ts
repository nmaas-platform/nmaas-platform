import { ConfigTemplate } from './configtemplate';

export class Application {
    constructor(public id: Number,
                
                public name: string,
                public version: string,
                
                public license: string,
                
                public briefDescription: string,
                public fullDescription: string,
                
                public tags:string[], 
    
                public configTemplate: ConfigTemplate) {
        
                
    }
}