import {AuthService} from '../../../auth/auth.service';
import {Domain} from '../../../model/domain';
import {User} from '../../../model/user';
import {Role, RoleAware} from '../../../model/userrole';
import { KeysPipe } from '../../../pipe/keys.pipe';
import {DomainService} from '../../../service/domain.service';
import {UserService} from '../../../service/user.service';
import { BaseComponent } from '../../common/basecomponent/base.component';
import {Component, OnInit, Input} from '@angular/core';
import {FormGroup, FormBuilder, Validators} from '@angular/forms';

@Component({
  selector: 'nmaas-userprivileges',
  templateUrl: './userprivileges.component.html',
  styleUrls: ['./userprivileges.component.css']
})
@RoleAware
export class UserPrivilegesComponent extends BaseComponent implements OnInit {

  @Input()
  private domainId: number;

  @Input()
  private user: User;

  private domains: Domain[] = [];
  private roles: Role[] = [];


  private newPrivilegeForm: FormGroup;

  constructor(protected fb: FormBuilder, protected domainService: DomainService,
    protected userService: UserService, protected authService: AuthService) {
    super();
    this.newPrivilegeForm = fb.group(
      {
        userId: [null, Validators.required],
        domainId: [null, Validators.required],
        role: [null, Validators.required]
      });

    this.roles = this.getAllowedRoles();
  }

  protected getAllowedRoles(): Role[] {
    let roles: Role[];

    if (this.authService.hasRole(Role[Role.ROLE_SUPERADMIN])) {
      roles = [Role.ROLE_SUPERADMIN, Role.ROLE_DOMAIN_ADMIN, Role.ROLE_TOOL_MANAGER, Role.ROLE_USER, Role.ROLE_GUEST];
    } else if (this.authService.hasRole(Role[Role.ROLE_DOMAIN_ADMIN])) {
      roles = [Role.ROLE_DOMAIN_ADMIN, Role.ROLE_USER, Role.ROLE_GUEST];
    } else {
      roles = [];
    }

    return roles;
  }

  ngOnInit() {
    if (this.domainId) {
      this.domainService.getOne(this.domainId).subscribe((domain) => this.domains.push(domain));
    } else {
      if (this.authService.hasRole(Role[Role.ROLE_SUPERADMIN])) {
        this.domainService.getAll().subscribe((domains) => this.domains = domains);
      } else if (this.authService.hasRole(Role[Role.ROLE_DOMAIN_ADMIN])) {
        const domainIds: number[] = this.authService.getDomainsWithRole(Role[Role.ROLE_DOMAIN_ADMIN]);
        domainIds.forEach((domainId) => {
          this.domainService.getOne(domainId).subscribe((domain) => this.domains.push(domain));
        });
      }
    }
  }

  protected add(): void {
    this.userService.addRole(this.user.id,
      Role[<string>(this.newPrivilegeForm.get('role').value)],
      this.newPrivilegeForm.get('domainId').value).subscribe(
        () => {
             this.newPrivilegeForm.reset();
             this.userService.getOne(this.user.id).subscribe((user) => this.user = user);
        });



  }

  protected remove(userId: number, role: Role, domainId?: number): void {
    this.userService.removeRole(userId, role, domainId).subscribe(
        () => this.userService.getOne(this.user.id).subscribe((user) => this.user = user))
  }
}
