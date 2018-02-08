import { User } from '../../../model/user';
import { UserService } from '../../../service/user.service';
import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-userdetails',
  templateUrl: './userdetails.component.html',
  styleUrls: ['./userdetails.component.css']
})
export class UserDetailsComponent implements OnInit {

  @Input()
  private user: User;

  constructor(private userService: UserService) { }

  ngOnInit() {
  }

  public onPasswordChange($password: string): void {
    this.userService.changePassword(this.user.id, $password)
  }
}
