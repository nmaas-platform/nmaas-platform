export enum AppViewType {
  APPLICATION,
  DOMAIN
}

export function AppViewTypeAware(constructor: Function) {
  constructor.prototype.AppViewType = AppViewType;
}
