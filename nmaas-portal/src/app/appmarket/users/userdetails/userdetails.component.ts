import {User} from '../../../model/user';
import {UserService} from '../../../service/user.service';
import {BaseComponent} from '../../../shared/common/basecomponent/base.component';
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {isUndefined} from 'util';
import {AuthService} from "../../../auth/auth.service";
import {TranslateService} from "@ngx-translate/core";
import {ContentDisplayService} from "../../../service/content-display.service";

@Component({
  selector: 'app-userdetails',
  templateUrl: './userdetails.component.html',
  styleUrls: ['./userdetails.component.css']
})
export class UserDetailsComponent extends BaseComponent implements OnInit {

  private userId: number;
  public user: User;
  public errorMessage: string;


  constructor(private userService: UserService, private router: Router,
    private route: ActivatedRoute, private location: Location, public authService:AuthService) {
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

      if (user.id) {
          this.userService.updateUser(user.id, user).subscribe(() => {
              this.errorMessage = undefined;
              this.router.navigate(['/users/view/', user.id])
          }, err => {
              this.errorMessage = err.message;
          });
      }
  }

  public remove(userId:number){
    this.userService.deleteOne(userId).subscribe((value) => this.router.navigate(['/users/']));
  }
}
