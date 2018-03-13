export enum ListType {
  GRID,
  TABLE
}

export function ListTypeAware(constructor: Function) {
  constructor.prototype.ListType = ListType;
}
