import {Component, OnInit} from '@angular/core';
import {BaseComponent} from '../../../../shared/common/basecomponent/base.component';
import {Router} from '@angular/router';
import {ConfigurationService} from '../../../../service';
import {Configuration} from '../../../../model/configuration';
import {InternationalizationService} from '../../../../service/internationalization.service';
import {Language} from '../../../../model/language';

@Component({
  selector: 'app-configurationdetails',
  templateUrl: './configurationdetails.component.html',
  styleUrls: ['./configurationdetails.component.css']
})
export class ConfigurationDetailsComponent extends BaseComponent implements OnInit {

  public errorMsg: string;
  public configuration: Configuration;
  public languages: Language[];

  constructor(private router: Router,
              private configurationService: ConfigurationService,
              private languageService: InternationalizationService) {
    super();
  }

  ngOnInit() {
      this.update();
      this.languageService.getAllSupportedLanguages().subscribe(langs => this.languages = langs);
  }

  public update(): void {
      this.configurationService.getConfiguration().subscribe(value => this.configuration = value, err => this.errorMsg = err.message);
  }

  public save(): void {
      this.configurationService.updateConfiguration(this.configuration).subscribe(() => this.update(), err => this.errorMsg = err.message);
  }

}
