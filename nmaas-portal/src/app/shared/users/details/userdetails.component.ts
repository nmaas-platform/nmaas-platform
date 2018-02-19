import {User} from '../../../model/user';
import { BaseComponent } from '../../common/basecomponent/base.component';
import {ComponentMode} from '../../common/componentmode';
import {Component, Input, Output, EventEmitter, OnInit} from '@angular/core';

@Component({
  selector: 'nmaas-userdetails',
  templateUrl: './userdetails.component.html',
  styleUrls: ['./userdetails.component.css']
})
export class UserDetailsComponent extends BaseComponent implements OnInit {

  @Input()
  private user: User = new User();

  @Output()
  private onSave: EventEmitter<User> = new EventEmitter<User>();

  constructor() {
    super();
  }

  ngOnInit() {

  }

  public submit(): void {
    console.log('submit(' + this.user.username + ')');
    this.onSave.emit(this.user);
  }

  public onModeChange(): void {
    const newMode: ComponentMode = (this.mode === ComponentMode.VIEW ? ComponentMode.EDIT : ComponentMode.VIEW);
    if (this.isModeAllowed(newMode)) {
      this.mode = newMode;
    }
  }


}
