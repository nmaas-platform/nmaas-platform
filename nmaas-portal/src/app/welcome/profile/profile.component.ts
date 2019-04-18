import {Component, OnInit} from '@angular/core';
import {ProfileService} from "../../service/profile.service";
import {User} from "../../model";
import {BaseComponent} from "../../shared/common/basecomponent/base.component";
import {TranslateService} from "@ngx-translate/core";
import {ContentDisplayService} from "../../service/content-display.service";
import {InternationalizationService} from "../../service/internationalization.service";
import {UserService} from "../../service";
import {Router} from "@angular/router";
import {ComponentMode} from "../../shared";

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
  public errorMessage: string;
  public userDetailsMode: ComponentMode = ComponentMode.VIEW;

  constructor(protected profileService:ProfileService, private translate: TranslateService, private router:Router,
              private contentService:ContentDisplayService, public userService: UserService) {
      super();
  }

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
    this.profileService.getOne().subscribe((user)=>this.user = user);
  }

  public onRefresh(){
    this.profileService.getOne().subscribe((user)=>{
        this.user = user;
        this.onModeChange();
        this.errorMessage = undefined;
    });
  }

  public onModeChange(){
      this.userDetailsMode = (this.userDetailsMode === ComponentMode.VIEW ? ComponentMode.EDIT : ComponentMode.VIEW);
  }

  public onSave($event) {
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
}
