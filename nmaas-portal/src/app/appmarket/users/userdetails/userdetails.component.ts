import {User} from '../../../model/user';
import {UserService} from '../../../service/user.service';
import {BaseComponent} from '../../../shared/common/basecomponent/base.component';
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {isUndefined} from 'util';
import {AuthService} from "../../../auth/auth.service";

@Component({
  selector: 'app-userdetails',
  templateUrl: './userdetails.component.html',
  styleUrls: ['./userdetails.component.css']
})
export class UserDetailsComponent extends BaseComponent implements OnInit {

  private userId: number;
  private user: User;

  constructor(private userService: UserService, private router: Router,
    private route: ActivatedRoute,
    private location: Location, private authService:AuthService) {
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

  public onSave($event) {
    const user: User = $event;

    if (!user) {
       return;
    }

    if(user.id) {
      this.userService.updateUser(user.id, user).subscribe((value) => this.router.navigate(['/users/view/', user.id]));
    }
  }

  public remove(userId:number){
    this.userService.deleteOne(userId).subscribe((value) => this.router.navigate(['/users/']));
  }
}
