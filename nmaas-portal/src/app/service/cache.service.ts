
export class CacheService<ID, OBJECT> {

  private cache: Map<ID, OBJECT> = new Map<ID, OBJECT>();
  
  constructor() { }

  public getData(id: ID): OBJECT {
    return this.cache.get(id);  
  }
  
  public setData(id: ID, obj: OBJECT): void {
    this.cache.set(id, obj);
  }
  
  public hasData(id: ID): boolean {
    return this.cache.has(id);
  }
}
