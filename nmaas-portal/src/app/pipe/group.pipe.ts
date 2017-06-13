import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'group', pure: true })
export class GroupPipe implements PipeTransform {
    public transform(value: Array<any>, by: number): Array<any> {
        console.debug("Group pipe: " + value + ', by: ' + by);
        if (!by)
            by = 1;

        let groups: Array<any> = [];
        if (!value)
            return groups;


        for (var i = 0; i < value.length; i += by) {
            groups.push(value.slice(i, i + by));
        }
        return groups;
    }
}