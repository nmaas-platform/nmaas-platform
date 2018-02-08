import {Pipe, PipeTransform} from '@angular/core';

@Pipe({ name: 'keys'})
export class KeysPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    const keys = [];
    for (const enumMember in value) {
      if (parseInt(enumMember, 10) >= 0) {
        keys.push({key: enumMember, value: value[enumMember]});
        console.log('enum member: ', value[enumMember]);
      }
    }
    return keys;
  }

}
