import {User} from '../../../model/user';
import {UserService} from '../../../service/user.service';
import {BaseComponent} from '../../../shared/common/basecomponent/base.component';
import {ComponentMode} from '../../../shared/common/componentmode';
import {Component, OnInit, Input} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Location} from '@angular/common';
import {isUndefined} from 'util';

@Component({
  selector: 'app-userdetails',
  templateUrl: './userdetails.component.html',
  styleUrls: ['./userdetails.component.css']
})
export class UserDetailsComponent extends BaseComponent implements OnInit {

  private userId: number;
  private user: User;

  protected mode: ComponentMode;

  constructor(private userService: UserService, private router: Router,
    private route: ActivatedRoute,
    private location: Location) {
    super();
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (!isUndefined(params['id'])) {
        this.userId = +params['id'];  
        this.userService.getOne(this.userId).subscribe((user) => this.user = user);
      }
    });
    this.mode = this.getMode(this.route);
  }

  protected getMode(route: ActivatedRoute): ComponentMode {
    if (isUndefined(route) || isUndefined(route.snapshot.data) || isUndefined(route.snapshot.data.mode)) {
      return ComponentMode.VIEW;
    } else {
      return route.snapshot.data.mode;
    }

  }

  public onPasswordSubmit($event): void {
    console.debug("Password submitted");
    this.userService.changePassword(this.user.id, $event).subscribe(() => {});
  }

  public onSave($event) {
    const user: User = $event;

    if (!user) {
       return;
    }

    this.userService.addUser(user.username).subscribe((id) => this.router.navigate(['/users/view/', id.id]));
  }
}
