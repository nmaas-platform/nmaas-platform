import {ComponentMode, ComponentModeAware} from '../componentmode';
import {Component, Input} from '@angular/core';

@Component({
  selector: 'nmaas-base',
  template: ''
})
@ComponentModeAware
export class BaseComponent {

  @Input()
  protected mode: ComponentMode = ComponentMode.VIEW;

  @Input()
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

  protected getCurrentMode(): ComponentMode {
    return this.mode;
  }
}