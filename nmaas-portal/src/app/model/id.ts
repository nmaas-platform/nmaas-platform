import {JsonObject, JsonProperty} from 'json2typescript';

@JsonObject
export class Id {

  @JsonProperty('id', Number)
  public id: Number = undefined;
  
  constructor(id?: Number) {
    this.id = id;
  }

}
