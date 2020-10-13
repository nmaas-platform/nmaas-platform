import {Component, OnInit, ViewChild} from '@angular/core';
import {InternationalizationService} from '../../../../service/internationalization.service';
import {Language} from '../../../../model/language';
import {TranslateService} from '@ngx-translate/core';
import {ModalComponent} from '../../../../shared/modal';

@Component({
  selector: 'app-languagelist',
  templateUrl: './languagelist.component.html',
  styleUrls: ['./languagelist.component.css']
})
export class LanguageListComponent implements OnInit {

  @ViewChild(ModalComponent, { static: true })
  public modal: ModalComponent;

  public languages: Language[] = [];

  constructor(public languageService: InternationalizationService,
              public translate: TranslateService) { }

  ngOnInit() {
    this.modal.setModalType('error');
    this.modal.setStatusOfIcons(false);
    this.languageService.getAllSupportedLanguages().subscribe(langs => this.languages = langs);
  }

  public changeLanguageState(language: Language) {
    if (language.language !== this.translate.currentLang && language.language !== this.translate.getDefaultLang()) {
      language.enabled = !language.enabled;
      this.languageService.changeSupportedLanguageState(language).subscribe(() => {
        this.languageService.setUpdateRequiredFlag(true);
      }, () => this.modal.show())
    } else {
      this.modal.show();
    }
  }

}
