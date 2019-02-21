import {Component, OnInit} from '@angular/core';
import {ProfileService} from "../../service/profile.service";
import {User} from "../../model";
import {BaseComponent} from "../../shared/common/basecomponent/base.component";
import {TranslateService} from "@ngx-translate/core";
import {ContentDisplayService} from "../../service/content-display.service";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
    providers:[ProfileService]
})
export class ProfileComponent extends BaseComponent implements OnInit {

  constructor(protected profileService:ProfileService, private translate: TranslateService,
              private contentService:ContentDisplayService) {
      super();
  }

  public user:User;
  public languages: string[];

  useLanguage(language: string) {
    this.translate.use(language);
  }

  getCurrent(){
    return this.translate.currentLang;
  }

  getPathToCurrent(){
    return "assets/images/country/" + this.getCurrent() + "_circle.png";
  }

  public getSupportedLanguages(){
    this.contentService.getLanguages().subscribe(langs =>{
      this.translate.addLangs(langs);
      this.languages = langs;
    });
  }

  ngOnInit() {
    this.getSupportedLanguages();
    this.profileService.getOne().subscribe((user)=>this.user = user)
  }

}
