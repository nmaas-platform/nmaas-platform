export enum ComponentMode {
  CREATE,
  VIEW,
  EDIT,
  DELETE,
  PROFILVIEW
}

export function ComponentModeAware(constructor: Function) {
    constructor.prototype.ComponentMode = ComponentMode;
}
