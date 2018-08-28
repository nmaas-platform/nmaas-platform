import {User} from '../../../model/user';
import { BaseComponent } from '../../common/basecomponent/base.component';
import {ComponentMode} from '../../common/componentmode';
import {Component, Input, Output, EventEmitter, OnInit} from '@angular/core';
import {AuthService} from "../../../auth/auth.service";

@Component({
  selector: 'nmaas-userdetails',
  templateUrl: './userdetails.component.html',
  styleUrls: ['./userdetails.component.css']
})
export class UserDetailsComponent extends BaseComponent implements OnInit {

  @Input()
  public user: User = new User();

  @Output()
  public onSave: EventEmitter<User> = new EventEmitter<User>();

  constructor(private authService:AuthService) {
    super();
  }

  ngOnInit() {

  }

  public submit(): void {
    console.log('submit(' + this.user.username + ')');
    this.onSave.emit(this.user);
    this.onModeChange();
  }

  public onModeChange(): void {
    const newMode: ComponentMode = (this.mode === ComponentMode.VIEW ? ComponentMode.EDIT : ComponentMode.VIEW);
    if (this.isModeAllowed(newMode)) {
      this.mode = newMode;
    }
  }


}
