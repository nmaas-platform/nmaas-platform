import { JsonConverter, JsonCustomConvert } from 'json2typescript';


@JsonConverter
export class DateConverter implements JsonCustomConvert<Date> {
  serialize(data: Date): any {
    console.log('DateConverter:serialize');
    return data.getTime();
  }
  deserialize(data: any): Date {
    console.log('DateConverter:deserialize');
    return new Date(data);
  }
}