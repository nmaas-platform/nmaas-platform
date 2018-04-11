import {AuthService} from '../auth/auth.service';
import {Directive, TemplateRef, ViewContainerRef, Input} from '@angular/core';

@Directive({
  selector: '[roles]',
  inputs: ['roles']
})
export class RolesDirective {

  constructor(private _templateRef: TemplateRef<any>,
    private _viewContainer: ViewContainerRef,
    private authService: AuthService) {

  }

  @Input() set roles(allowedRoles: Array<string>) {
    let show: boolean = false;
    for (let allowedRole of allowedRoles) {
      if (this.authService.hasRole(allowedRole)) {
        show = true;
        break;
      }
    }

    if (show) {
      this._viewContainer.createEmbeddedView(this._templateRef);
    } else {
      this._viewContainer.clear();
    }
  }
}
