import {JsonObject, JsonProperty} from 'json2typescript';

@JsonObject
export class ConfigTemplate {

  @JsonProperty('template', String)
  public template: string;
}
