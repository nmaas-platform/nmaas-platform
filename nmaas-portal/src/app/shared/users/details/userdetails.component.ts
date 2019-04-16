import {User} from '../../../model/user';
import {BaseComponent} from '../../common/basecomponent/base.component';
import {ComponentMode} from '../../common/componentmode';
import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import {AuthService} from "../../../auth/auth.service";
import {PasswordComponent} from "../../common/password/password.component";

@Component({
  selector: 'nmaas-userdetails',
  templateUrl: './userdetails.component.html',
  styleUrls: ['./userdetails.component.css']
})
export class UserDetailsComponent extends BaseComponent implements OnInit {

  @ViewChild(PasswordComponent)
  public readonly passwordModal:PasswordComponent;

  @Input()
  public user: User = new User();

  @Input()
  public errorMessage: string;

  @Output()
  public onSave: EventEmitter<User> = new EventEmitter<User>();

  @Output()
  public refresh: EventEmitter<any> = new EventEmitter();

  constructor(private authService:AuthService) {
    super();
  }

  ngOnInit() {

  }

  public submit() {
    console.log('submit(' + this.user.username + ')');
    this.onSave.emit(this.user);
  }

  public onModeChange(): void {
    const newMode: ComponentMode = (this.mode === ComponentMode.VIEW ? ComponentMode.EDIT : ComponentMode.VIEW);
    if (this.isModeAllowed(newMode)) {
      this.mode = newMode;
      this.errorMessage = undefined;
      this.refresh.emit();
    }
  }

  public canChangePassword(): boolean {
    return this.user.username === this.authService.getUsername();
  }

}
