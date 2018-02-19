import {Injectable} from '@angular/core';
import {JsonConvert, JsonConverter, JsonCustomConvert, OperationMode, ValueCheckingMode} from 'json2typescript';


@Injectable()
export class JsonMapperService {

  protected jsonConverter: JsonConvert;

  constructor() {
    this.jsonConverter = new JsonConvert();
    this.jsonConverter.operationMode = OperationMode.LOGGING;
    this.jsonConverter.ignorePrimitiveChecks = false;
    this.jsonConverter.valueCheckingMode = ValueCheckingMode.DISALLOW_NULL; //ALLOW_OBJECT_NULL;
  }

  public deserialize(json: any, classReference: { new(): any }): any {
    return this.jsonConverter.deserialize(json, classReference);
  }

  public serialize(obj: any): any {
    return this.jsonConverter.serialize(obj);
  }
}
