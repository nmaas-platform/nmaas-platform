import {TemplateRef, ViewContainerRef} from "@angular/core";
import {Role} from "../model/userrole";
import {DomainRolesDirective} from "./domain-roles.directive";
import {AuthService} from "../auth/auth.service";

describe('Domain Roles Directive test', () => {

    let authServiceStub;
    let viewContainerStub: ViewContainerRef;
    let templateRefStub: TemplateRef<any>;

    beforeEach(() => {
        viewContainerStub = jasmine.createSpyObj('ViewContainerRed',['clear', 'createEmbeddedView'])
        authServiceStub = jasmine.createSpyObj('AuthService',['hasRole', 'hasDomainRole']);

    });

    it("states should be equal when domain id and role set matches", () => {
        let domainRolesDirective = new DomainRolesDirective(templateRefStub, viewContainerStub, authServiceStub);
        const roles = new Set([Role[Role.ROLE_SYSTEM_ADMIN],]);
        let result = domainRolesDirective.compareStates({domainId: 1, roles: roles }, {domainId: 1, roles: roles});
        expect(result).toEqual(true);
        result = domainRolesDirective.compareStates({domainId: 2, roles: roles}, {domainId: 3, roles:roles});
        expect(result).toEqual(false);
        const roles2 = new Set([Role[Role.ROLE_USER], Role[Role.ROLE_DOMAIN_ADMIN]]);
        result = domainRolesDirective.compareStates({domainId: 1, roles: roles}, {domainId: 1, roles: roles2});
        expect(result).toEqual(false)
    });

    it("access should be granted when user is domain admin", () => {
        authServiceStub.hasRole.and.callFake((arg: string) => {
            return arg === Role[Role.ROLE_SYSTEM_ADMIN];
        });
        authServiceStub.hasDomainRole.and.returnValue(false);
        let domainRolesDirective = new DomainRolesDirective(templateRefStub, viewContainerStub, authServiceStub);
        domainRolesDirective.domainRolesDomainId = 1;
        domainRolesDirective.domainRoles = [Role[Role.ROLE_SYSTEM_ADMIN]];
        expect(viewContainerStub.createEmbeddedView).toHaveBeenCalledTimes(1);
        expect(viewContainerStub.clear).toHaveBeenCalledTimes(3);
    });

    it("has access in one domain but not in other one", () => {
        authServiceStub.hasRole.and.returnValue(false);
        authServiceStub.hasDomainRole.and.callFake((id: number, role: string) => {
            if(id === 1 && role === Role[Role.ROLE_DOMAIN_ADMIN]) {
                return true
            }
            if(id === 2 && role === Role[Role.ROLE_DOMAIN_ADMIN]) {
                return false;
            }
            return false;
        });
        let domainRolesDirective = new DomainRolesDirective(templateRefStub, viewContainerStub, authServiceStub);
        domainRolesDirective.domainRolesDomainId = 1;
        domainRolesDirective.domainRoles = [Role[Role.ROLE_DOMAIN_ADMIN]];
        expect(viewContainerStub.createEmbeddedView).toHaveBeenCalledTimes(1);
        domainRolesDirective.domainRolesDomainId = 2;
        expect(viewContainerStub.createEmbeddedView).toHaveBeenCalledTimes(1); // Nothing changed
        expect(viewContainerStub.clear).toHaveBeenCalledTimes(5);
    })

});
