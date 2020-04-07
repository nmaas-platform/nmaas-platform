import {AuthService} from '../../../auth/auth.service';
import {Domain} from '../../../model/domain';
import {User} from '../../../model/user';
import {Role, RoleAware} from '../../../model/userrole';
import {DomainService} from '../../../service/domain.service';
import {UserService} from '../../../service/user.service';
import {BaseComponent} from '../../common/basecomponent/base.component';
import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Observable, of} from 'rxjs';
import {CacheService} from '../../../service/cache.service';
import {UserDataService} from '../../../service/userdata.service';
import {isNullOrUndefined} from 'util';
import {map, shareReplay, take} from 'rxjs/operators';

@Component({
  selector: 'nmaas-userprivileges',
  templateUrl: './userprivileges.component.html',
  styleUrls: ['./userprivileges.component.css']
})
@RoleAware
export class UserPrivilegesComponent extends BaseComponent implements OnInit {

  public domainId: number;

  @Input()
  public user: User;

  public domains: Domain[] = [];
  public roles: Role[] = [];

  public domainCache: CacheService<number, Domain> = new CacheService<number, Domain>();

  public newPrivilegeForm: FormGroup;

  constructor(protected fb: FormBuilder, protected domainService: DomainService,
    protected userService: UserService, public authService: AuthService, protected userData: UserDataService) {
    super();
    this.newPrivilegeForm = fb.group(
      {
        userId: [null, Validators.required],
        domainId: [null, Validators.required],
        role: [null, Validators.required]
      });

    this.roles = this.getAllowedRoles();
    userData.selectedDomainId.subscribe(value => {
        this.domainId = value;
        this.newPrivilegeForm.get('domainId').setValue(this.domainId);
    });
  }

  public getAllowedRoles(): Role[] {
    let roles: Role[];
    if (this.authService.hasRole(Role[Role.ROLE_SYSTEM_ADMIN]) &&
        Number(this.newPrivilegeForm.get('domainId').value) === this.domainService.getGlobalDomainId()) {
        // console.debug('Admin global roles set');
      roles = [Role.ROLE_OPERATOR, Role.ROLE_TOOL_MANAGER, Role.ROLE_SYSTEM_ADMIN];
      roles = this.filterRoles(roles, this.newPrivilegeForm.get('domainId').value);
    } else if (this.newPrivilegeForm.get('domainId').value != null) {
        // console.debug('Standard roles set');
      roles = [Role.ROLE_GUEST, Role.ROLE_USER, Role.ROLE_DOMAIN_ADMIN ];
      roles = this.filterRoles(roles, this.newPrivilegeForm.get('domainId').value);
    } else {
        // console.debug('Empty roles set');
      roles = [];
    }
    return roles;
  }

  private filterRoles(roles: Role[], domainId: number): Role[] {
      const role = this.user.roles.find(value => value.domainId == domainId);
      if (isNullOrUndefined(role)) {
          return roles;
      }
      return roles.filter(value => Role[value] != role.role.toString());
  }

  ngOnInit() {
      if (this.authService.hasRole(Role[Role.ROLE_SYSTEM_ADMIN])) {
        this.domainService.getAll().subscribe((domains) => this.domains = domains);
      } else if (this.authService.hasRole(Role[Role.ROLE_DOMAIN_ADMIN])) {
        const domainIds: number[] = this.authService.getDomainsWithRole(Role[Role.ROLE_DOMAIN_ADMIN]);
        domainIds.forEach((domainId) => {
          this.domainService.getOne(domainId).subscribe((domain) => this.domains.push(domain));
        });
      }
  }

  public add(): void {
    this.userService.addRole(this.user.id,
      Role[<string>(this.newPrivilegeForm.get('role').value)],
      this.newPrivilegeForm.get('domainId').value).subscribe(
        () => {
             this.newPrivilegeForm.reset();
             this.userService.getOne(this.user.id).subscribe((user) => this.user = user);
             this.newPrivilegeForm.get('domainId').setValue(this.domainId);
        });
  }

  public remove(userId: number, role: Role, domainId?: number): void {
    this.userService.removeRole(userId, role, domainId).subscribe(
        () => this.userService.getOne(this.user.id).subscribe((user) => this.user = user))
  }

  public getDomainName(domainId: number): Observable<string> {
        if (this.domainCache.hasData(domainId)) {
            return of(this.domainCache.getData(domainId).name);
        } else {
            return this.domainService.getOne(domainId).pipe(
                map((domain) => {this.domainCache.setData(domainId, domain); return domain.name}),
                shareReplay(1),
                take(1));
        }
  }
}
