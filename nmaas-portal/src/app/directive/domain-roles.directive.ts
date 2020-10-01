import {Directive, Input, TemplateRef, ViewContainerRef} from '@angular/core';
import {AuthService} from '../auth/auth.service';
import {Role} from '../model/userrole';

/*
this class represents inner state of directive
 */
class InnerState {
    public roles: Set<string> = new Set<string>();
    public domainId = -1;
}

@Directive({
    selector: '[domainRoles]'
})
export class DomainRolesDirective {

    @Input() set domainRoles(value: string[]) {
        this._roles = new Set<string>(value);
        this.handleStateChange({
            roles: this._roles,
            domainId: this._domainId,
        });
    }

    @Input() set domainRolesDomainId(value: number) {
        this._domainId = value;
        this.handleStateChange({
            roles: this._roles,
            domainId: this._domainId,
        })
    }

    private state: InnerState = new InnerState();

    private _roles: Set<string> = new Set<string>();

    private _domainId: number;

    constructor(private _templateRef: TemplateRef<any>,
                private _viewContainer: ViewContainerRef,
                private authService: AuthService) {

    }

    /*
    returns true if states are equal, otherwise returns false
     */
    compareStates(a: InnerState, b: InnerState): boolean {
        if (a.domainId !== b.domainId) {
            return false;
        }

        const difference = new Set(a.roles);
        for (const element of b.roles) {
            difference.delete(element);
        }

        return difference.size === 0;
    }

    /*
    display resolving shall be applied only when directive state has changed,
     */
    handleStateChange(newState: InnerState) {
        const statesAreEqual = this.compareStates(this.state, newState);
        if (!statesAreEqual) {
            this.resolve();
        }
    }

    resolve() {
        this._viewContainer.clear(); // clear container first to avoid doubling display
        let show = false;
        if (this._domainId || this._roles) {
            if (this._roles.has(Role[Role.ROLE_SYSTEM_ADMIN]) && this.authService.hasRole(Role[Role.ROLE_SYSTEM_ADMIN])) {
                show = true;
            }
            for (const allowedRole of this._roles) {
                if (this.authService.hasDomainRole(this._domainId, allowedRole)) {
                    show = true;
                    break;
                }
            }
        }

        if (show) {
            this._viewContainer.createEmbeddedView(this._templateRef);
        } else {
            this._viewContainer.clear();
        }
    }


}
