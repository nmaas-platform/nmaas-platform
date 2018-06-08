import {Component, OnInit} from '@angular/core';
import {ProfileService} from "../../service/profile.service";
import {User} from "../../model";
import {BaseComponent} from "../../shared/common/basecomponent/base.component";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
    providers:[ProfileService]
})
export class ProfileComponent extends BaseComponent implements OnInit {

  constructor(protected profileService:ProfileService) {super()}

  private user:User;

  ngOnInit() {
    this.profileService.getOne().subscribe((user)=>this.user = user)
  }

}
