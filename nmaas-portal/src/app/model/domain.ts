import { Id } from './id';

export class Domain {
  
  constructor();  
  constructor(public id?: Number,
              public name?: string,
              public active?: boolean) { }
}
