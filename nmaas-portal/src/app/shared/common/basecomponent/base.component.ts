import {ComponentMode} from '../componentmode';
import {Component, Input} from '@angular/core';

@Component({
  selector: 'nmaas-base',
  template: ''
})
export class BaseComponent {

  protected ComponentMode = ComponentMode;

  @Input()
  protected mode: ComponentMode = ComponentMode.VIEW;

  protected allowedModes: ComponentMode[] = [ComponentMode.VIEW];

  constructor() {}

  protected isModeAllowed(mode: ComponentMode): boolean {
    return this.allowedModes.indexOf(mode) >= 0;
  }

  protected isCurrentModeAllowed(): boolean {
    return this.isModeAllowed(this.mode);
  }

  protected isInMode(mode: ComponentMode): boolean {
    return this.mode === mode;
  }
}
