import { Id } from './id';
import {JsonObject, JsonProperty} from 'json2typescript';


@JsonObject
export class Domain {
  
  @JsonProperty('id', Number)
  public id: number = undefined;
  
  @JsonProperty('name', String)
  public name: string = undefined;
  
  @JsonProperty('active', Boolean)
  public active: boolean = undefined
  
  constructor();  
  constructor(id?: number,
              name?: string,
              active?: boolean) { 
    this.id = id;
    this.name = name;
    this.active = active;
  }
}
