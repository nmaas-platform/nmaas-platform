import {Component, OnInit} from '@angular/core';
import {ProfileService} from "../../service/profile.service";
import {User} from "../../model";
import {BaseComponent} from "../../shared/common/basecomponent/base.component";
import {TranslateService} from "@ngx-translate/core";
import {ContentDisplayService} from "../../service/content-display.service";
import {InternationalizationService} from "../../service/internationalization.service";
import {UserService} from "../../service";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
    providers:[ProfileService]
})
export class ProfileComponent extends BaseComponent implements OnInit {

  constructor(protected profileService:ProfileService, private translate: TranslateService,
              private contentService:ContentDisplayService,
              public userService: UserService,
              private languageService:InternationalizationService) {
      super();
  }

  public user:User;
  public languages: string[];

  setLanguage(language: string) {
    this.userService.setUserLanguage(this.user.id, language).subscribe(() => {
      this.user.selectedLanguage = language;
      localStorage.setItem('lang', language);
      this.translate.use(language);
    });
  }

  getPathToCurrent(){
    return "assets/images/country/" + this.user.selectedLanguage + "_circle.png";
  }

  public getSupportedLanguages(){
    this.languageService.getEnabledLanguages().subscribe(langs =>{
      this.translate.addLangs(langs);
      this.languages = langs;
    });
  }

  ngOnInit() {
    this.getSupportedLanguages();
    this.profileService.getOne().subscribe((user)=>this.user = user)
  }

}
