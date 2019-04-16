import {User} from '../../../model/user';
import {UserService} from '../../../service/user.service';
import {BaseComponent} from '../../../shared/common/basecomponent/base.component';
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {isUndefined} from 'util';
import {AuthService} from "../../../auth/auth.service";
import {ComponentMode} from "../../../shared";

@Component({
  selector: 'app-userdetails',
  templateUrl: './userdetails.component.html',
  styleUrls: ['./userdetails.component.css']
})
export class UserDetailsComponent extends BaseComponent implements OnInit {

  private userId: number;
  public user: User;
  public errorMessage: string;
  public userDetailsMode: ComponentMode = ComponentMode.VIEW;


  constructor(private userService: UserService, private router: Router,
    private route: ActivatedRoute, public authService:AuthService) {
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

    public onRefresh(){
        this.userService.getOne(this.userId).subscribe((user)=>this.user = user);
    }

    public onSave($event) {
        this.userDetailsMode = ComponentMode.EDIT;
        const user: User = $event;

        if (!user) {
            return;
        }

        if (user.id) {
            return this.updateUser(user.id, user);
        }
    }

    async updateUser(userId: number, user: User) {
        return await Promise.resolve(this.userService.updateUser(userId, user).toPromise()
            .then(()=> {
                this.userDetailsMode = ComponentMode.VIEW;
                this.errorMessage = undefined;
            })
            .catch(err => {
                this.userDetailsMode = ComponentMode.EDIT;
                this.errorMessage = err.message;
            }));
    }

  public remove(userId:number){
    this.userService.deleteOne(userId).subscribe(() => this.router.navigate(['/users/']));
  }
}
