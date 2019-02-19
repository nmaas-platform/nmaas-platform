import {Component, Input, OnInit} from '@angular/core';
import {Application} from "../../../model";
import {ActivatedRoute} from "@angular/router";
import {isNullOrUndefined} from "util";
import {AppImagesService, AppsService} from "../../../service";
import {AppDescription} from "../../../model/appdescription";
import {TranslateService} from "@ngx-translate/core";

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
              public appImagesService:AppImagesService) { }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if(!isNullOrUndefined(params['id'])) {
        this.appService.getApp(params['id']).subscribe(result => this.app = result);
      }
    });
  }

  public getDescription(): AppDescription {
    if(isNullOrUndefined(this.app)){
      return;
    }
    return this.app.descriptions.find(val => val.language == this.translate.currentLang);
  }

}
