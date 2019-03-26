import { Component, OnInit } from '@angular/core';
import {InternationalizationService} from "../../../../service/internationalization.service";
import {ActivatedRoute, Router} from "@angular/router";
import {isNullOrUndefined} from "util";
import {Language} from "../../../../model/language";
import {MailTemplateService} from "../../../../service/mailtemplate.service";
import {MailTemplate} from "../../../../model/mailtemplate";
import {LanguageMailContent} from "../../../../model/languagemailcontent";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-languagedetails',
  templateUrl: './languagedetails.component.html',
  styleUrls: ['./languagedetails.component.css']
})
export class LanguageDetailsComponent implements OnInit {

  public language: Language;
  public mailTemplates: MailTemplate[] = [];

  public languageContent: any;

  public keys: string[] = [];
  public hide: boolean[] = [];
  public hideMails: boolean[] = [];

  public advanced: boolean = false;
  public switchLabel: string;

  constructor(public languageService: InternationalizationService, public mailTemplateService: MailTemplateService,
              public translate: TranslateService, public route: ActivatedRoute, public router: Router) { }

  ngOnInit() {
    this.handleLabel();
    this.route.params.subscribe(param =>{
      if(!isNullOrUndefined(param['id'])){
        this.languageService.getLanguage(param['id']).subscribe(lang => {
            this.language = lang;
            this.languageContent = JSON.parse(lang.content);
            this.keys = this.getKeys(this.languageContent);
            this.keys.forEach(() => this.hide.push(true));
            this.mailTemplateService.getTemplates().subscribe(templates => {
                this.mailTemplates = templates;
                templates.forEach(() => this.hideMails.push(true));
            });
        });
      }
    });
  }

  public getKeys(content: string){
      return Object.keys(content);
  }

  public isObject(key: any){
      return typeof key === 'object';
  }

  public handleLabel() {
      this.switchLabel =  this.advanced === true ? 'Raw mode is enabled' : 'Raw mode is disabled'; //TODO: Add translations
  }

  public getTemplateInSelectedLang(templates: LanguageMailContent[]) : LanguageMailContent {
      return templates.find(template => template.language === this.language.language);
  }

  public saveChanges(){
      if(!this.advanced){
          this.prepareData();
      }
      this.languageService.saveLanguageContent(this.language).subscribe(() => {
          this.translate.reloadLang(this.language.language).subscribe(() => console.debug("Language reloaded"));
          this.mailTemplateService.saveTemplates(this.mailTemplates).subscribe(() => console.debug("Mail templates saved"));
          this.router.navigate(['/admin/languages']);
      })
  }

  public prepareData(){
      this.language.content = this.languageContent;
  }

}
