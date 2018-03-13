import {ConfigTemplate} from './configtemplate';
import {JsonObject, JsonProperty} from 'json2typescript';

@JsonObject
export class Application {

  @JsonProperty('id', Number)
  public id: number = undefined;

  @JsonProperty('name', String)
  public name: string = undefined;

  @JsonProperty('version', String)
  public version: string = undefined;

  @JsonProperty('license', String)
  public license: string = undefined;

  @JsonProperty('wwwUrl', String)
  public wwwUrl: string = undefined;

  @JsonProperty('sourceUrl', String)
  public sourceUrl: string = undefined;

  @JsonProperty('issuesUrl', String)
  public issuesUrl: string = undefined;

  @JsonProperty('briefDescription', String)
  public briefDescription: string = undefined;

  @JsonProperty('fullDescription', String, true)
  public fullDescription: string = undefined;

  @JsonProperty('tags', [String])
  public tags: string[] = [];

  @JsonProperty('configTemplate', ConfigTemplate, true)
  public configTemplate: ConfigTemplate = undefined;

}
