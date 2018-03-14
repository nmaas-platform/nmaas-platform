import {JsonObject, JsonProperty, JsonConverter, JsonCustomConvert} from 'json2typescript';

@JsonConverter
export class TemplateConverter implements JsonCustomConvert<string> {
  serialize(data: string): any {
    console.debug('TemplateConverter:serialize');
    return JSON.parse(data.toString());
  }
  deserialize(data: any): string {
    console.debug('TemplateConverter:deserialize');
    return JSON.stringify(data);
  }
}

@JsonObject
export class ConfigTemplate {

  @JsonProperty('template', TemplateConverter)
  public template: string = undefined;
}
