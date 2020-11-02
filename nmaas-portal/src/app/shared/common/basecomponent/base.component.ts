import {ComponentMode} from '../componentmode';
import {Component, Input} from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'nmaas-base',
  template: ''
})
export class BaseComponent {

  public ComponentMode = ComponentMode;

  @Input()
  public mode: ComponentMode = ComponentMode.VIEW;

  @Input()
  public allowedModes: ComponentMode[] = [ComponentMode.VIEW];

  constructor() {}

  public isModeAllowed(mode: ComponentMode): boolean {
    return this.allowedModes.indexOf(mode) >= 0;
  }

  public isCurrentModeAllowed(): boolean {
    return this.isModeAllowed(this.mode);
  }

  public isInMode(mode: ComponentMode): boolean {
    return this.mode === mode;
  }

  public getCurrentMode(): ComponentMode {
    return this.mode;
  }

  public getMode(route: ActivatedRoute): ComponentMode {
    if (route === undefined || route.snapshot.data === undefined || route.snapshot.data.mode === undefined) {
      return ComponentMode.VIEW;
    } else {
      return route.snapshot.data.mode;
    }
  }
}
