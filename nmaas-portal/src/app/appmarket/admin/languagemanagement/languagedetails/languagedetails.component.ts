import {Component, OnInit} from '@angular/core';
import {InternationalizationService} from '../../../../service/internationalization.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Language} from '../../../../model/language';
import {MailTemplateService} from '../../../../service/mailtemplate.service';
import {MailTemplate} from '../../../../model/mailtemplate';
import {LanguageMailContent} from '../../../../model/languagemailcontent';
import {TranslateService} from '@ngx-translate/core';

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
    public newKeys: string[] = [];
    public newValues: string[] = [];
    public newNestedKeys: any[] = [];
    public newNestedValues: any[] = [];
    public errorMsg: string;
    public formErrorMsg: string;

    public advanced = false;
    public switchLabel: string;

    constructor(public languageService: InternationalizationService, public mailTemplateService: MailTemplateService,
                public translate: TranslateService, public route: ActivatedRoute, public router: Router) {
    }

    ngOnInit() {
        this.handleLabel();
        this.route.params.subscribe(param => {
            if (param['id'] != null) {
                this.languageService.getLanguage(param['id']).subscribe(
                    lang => {
                        this.language = lang;
                        this.languageContent = JSON.parse(lang.content);
                        this.keys = this.getKeys(this.languageContent);
                        this.keys.forEach(key => {
                            this.hide.push(true);
                            this.newKeys.push('');
                            this.newValues.push('');
                            this.newNestedKeys.push([]);
                            this.newNestedValues.push([]);
                            this.getKeys(key).forEach(value => {
                                if (this.isObject(value)) {
                                    this.newNestedKeys[this.newNestedKeys.length - 1].push('');
                                    this.newNestedValues[this.newNestedValues.length - 1].push('');
                                }
                            })
                        });
                        this.mailTemplateService.getTemplates().subscribe(templates => {
                            this.mailTemplates = templates;
                            templates.forEach(() => this.hideMails.push(true));
                        });
                    },
                    err => {
                        console.error(err);
                        if (err.statusCode &&
                            (err.statusCode === 404 || err.statusCode === 401 || err.statusCode === 403 || err.statusCode === 500)) {
                            this.router.navigateByUrl('/notfound');
                        }
                    });
            }
        });
    }

    public getKeys(content: string) {
        return Object.keys(content);
    }

    public isObject(key: any) {
        return typeof key === 'object';
    }

    public handleLabel() {
        this.switchLabel = this.advanced === true ? this.translate.instant('LANGUAGE_MANAGEMENT.RAW_MODE_ENABLED') : this.translate.instant('LANGUAGE_MANAGEMENT.RAW_MODE_DISABLED');
    }

    public getTemplateInSelectedLang(templates: LanguageMailContent[]): LanguageMailContent {
        return templates.find(template => template.language === this.language.language);
    }

    public saveChanges() {
        if (!this.advanced) {
            this.prepareData();
        }
        this.languageService.saveLanguageContent(this.language).subscribe(() => {
            this.formErrorMsg = undefined;
            this.translate.reloadLang(this.language.language).subscribe(() => console.debug('Language reloaded'));
            this.mailTemplateService.saveTemplates(this.mailTemplates).subscribe(() => console.debug('Mail templates saved'));
            this.router.navigate(['/admin/languages']);
        }, error => this.formErrorMsg = error.message);
    }

    public prepareData() {
        this.language.content = this.languageContent;
    }

    public handleAddingNewElements(element: any, index: number, nestedIndex: number) {
        if (nestedIndex == null) {
            this.addNewElement(element, index)
        } else {
            this.addNewNestedElement(element, index, nestedIndex);
        }
    }

    public addNewElement(element: any, index: number) {
        if (!element.hasOwnProperty(this.newKeys[index])) {
            element[this.newKeys[index]] = this.newValues[index];
            this.errorMsg = undefined;
            this.newKeys[index] = '';
            this.newValues[index] = '';
        } else {
            this.errorMsg = this.translate.instant('LANGUAGE_MANAGEMENT.KEY_EXISTS_MESSAGE');
        }
    }

    public addNewNestedElement(element: any, index: number, nestedIndex: number) {
        if (!element.hasOwnProperty(this.newNestedKeys[index][nestedIndex])) {
            element[this.newNestedKeys[index][nestedIndex]] = this.newNestedValues[index][nestedIndex];
            this.newNestedKeys[index][nestedIndex] = '';
            this.newNestedValues[index][nestedIndex] = '';
            this.errorMsg = undefined;
        } else {
            this.errorMsg = this.translate.instant('LANGUAGE_MANAGEMENT.KEY_EXISTS_MESSAGE');
        }
    }

}
