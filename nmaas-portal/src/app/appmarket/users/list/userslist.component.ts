import {AuthService} from '../../../auth/auth.service';
import {DomainService} from '../../../service/domain.service';
import {UserService} from '../../../service/user.service';
import {Component, OnInit} from '@angular/core';


@Component({
  selector: 'app-userslist',
  templateUrl: './userslist.component.html',
  styleUrls: ['./userslist.component.css']
})
export class UsersListComponent implements OnInit {

  constructor(protected authService: AuthService, protected userService: UserService, protected domainService: DomainService) {}

  ngOnInit() {

  }

}
