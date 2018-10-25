import {Component, OnInit} from '@angular/core';
import {ProfileService} from "../../service/profile.service";
import {User} from "../../model";
import {BaseComponent} from "../../shared/common/basecomponent/base.component";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
    providers:[ProfileService]
})
export class ProfileComponent extends BaseComponent implements OnInit {

  constructor(protected profileService:ProfileService, private translate: TranslateService) {
      super();
      const browserLang = translate.currentLang == null ? 'en' : translate.currentLang;
      translate.use(browserLang.match(/en|fr|pl/) ? browserLang : 'en');
  }

  public user:User;

  ngOnInit() {
    this.profileService.getOne().subscribe((user)=>this.user = user)
  }

}
