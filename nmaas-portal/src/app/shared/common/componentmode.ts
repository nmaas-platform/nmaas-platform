export enum ComponentMode {
  CREATE,
  VIEW,
  EDIT,
  DELETE
}

export function ComponentModeAware(constructor: Function) {
    constructor.prototype.ComponentMode = ComponentMode;
}
