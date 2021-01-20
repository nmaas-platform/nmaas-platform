import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {User} from '../../../model';
import {BaseComponent} from '../../common/basecomponent/base.component';
import {DomainService} from '../../../service';
import {Role, UserRole} from '../../../model/userrole';

function toEnum(role: string | Role): Role {
  if (typeof role === 'string') {
    return Role[role];
  }
  return role;
}

@Component({
  selector: 'nmaas-preferences',
  templateUrl: './preferences.component.html',
  styleUrls: ['./preferences.component.css']
})
export class PreferencesComponent extends BaseComponent implements OnInit {

  public myDomainNames: Map<number, string> = new Map<number, string>();

  @Input()
  public user: User = new User();

  public _errorMessage: string;

  @Output()
  public errorMessageChange: EventEmitter<any> = new EventEmitter();

  @Output()
  public onSave: EventEmitter<User> = new EventEmitter<User>();

  @Output()
  public refresh: EventEmitter<any> = new EventEmitter();

  @Output()
  public userDetailsModeChange: EventEmitter<any> = new EventEmitter();

  @Input()
  get errorMessage() {
    return this._errorMessage;
  }

  set errorMessage(val) {
    this._errorMessage = val;
  }

  @Input()
  get userDetailsMode() {
    return this.mode;
  }

  set userDetailsMode(val) {
    this.mode = val;
    this.userDetailsModeChange.emit(this.mode);
  }

  constructor( public domainService: DomainService) {
    super();
  }

  ngOnInit(): void {
    this.domainService.getMyDomains().subscribe(
        domains => domains.forEach(d => this.myDomainNames.set(d.id, d.name))
    )
  }

  public submit() {
    this.onSave.emit(this.user);
  }

  public onModeChange(): void {
    this.refresh.emit();
  }

  public getNameForDomain(id: number): string {
    return this.myDomainNames.get(id);
  }

  public getFilteredUserRoles(): UserRole[] {
    const globalDomainId = this.domainService.getGlobalDomainId();
    return this.user.roles.filter(ur => ur.domainId !== globalDomainId || toEnum(ur.role) === Role.ROLE_SYSTEM_ADMIN)
  }

}
