import {Component, Input, OnInit} from '@angular/core';
import {Application} from "../../../model";
import {ActivatedRoute} from "@angular/router";
import {isNullOrUndefined} from "util";
import {AppImagesService, AppsService} from "../../../service";
import {AppDescription} from "../../../model/appdescription";
import {TranslateService} from "@ngx-translate/core";
import {DomSanitizer} from "@angular/platform-browser";

@Component({
  selector: 'app-apppreview',
  templateUrl: './apppreview.component.html',
  styleUrls: ['./apppreview.component.css']
})
export class AppPreviewComponent implements OnInit {

  @Input()
  public app:Application;

  @Input()
  public logo:any;

  @Input()
  public screenshots:any[];

  constructor(public route:ActivatedRoute, public appService: AppsService, public translate:TranslateService,
              public appImagesService:AppImagesService, public dom:DomSanitizer) { }

  ngOnInit() {
    if(!this.app){
      this.route.params.subscribe(params => {
        if(!isNullOrUndefined(params['id'])) {
          this.appService.getApp(params['id']).subscribe(result => {
            this.app = result;
            this.getLogo(result.id);
          });
        }
      });
    }
  }

  public getLogo(id:number) : void {
    this.appImagesService.getLogoFile(id).subscribe(file => {
      this.logo = this.convertToProperImageFile(file);
    }, err => console.debug(err.message));
  }

  private convertToProperImageFile(file:any) {
    let result: any = new File([file], 'uploaded file', {type: file.type});
    result.objectURL = this.dom.bypassSecurityTrustUrl(URL.createObjectURL(result));
    return result;
  }

  public getDescription(): AppDescription {
    if(isNullOrUndefined(this.app)){
      return;
    }
    return this.app.descriptions.find(val => val.language == this.translate.currentLang);
  }

  public getPathUrl(id: number): string{
    if(!isNullOrUndefined(id) && !isNaN(id)){
      return '/apps/' + id + '/rate/my';
    }else{
      return "";
    }
  }

}
