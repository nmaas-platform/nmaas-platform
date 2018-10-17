import {Component, OnInit} from '@angular/core';
import {BaseComponent} from '../../../../shared/common/basecomponent/base.component';
import {Router} from '@angular/router';
import {ConfigurationService} from '../../../../service';
import {Configuration} from '../../../../model/configuration';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-configurationdetails',
  templateUrl: './configurationdetails.component.html',
  styleUrls: ['./configurationdetails.component.css']
})
export class ConfigurationDetailsComponent extends BaseComponent implements OnInit {

  public errorMsg: string;
  public configuration: Configuration;

  constructor(private router: Router, private configurationService: ConfigurationService, private translate: TranslateService) {
    super();
    const browserLang = translate.currentLang == null ? 'en' : translate.currentLang;
    translate.use(browserLang.match(/en|fr|pl/) ? browserLang : 'en');
  }

  ngOnInit() {
      this.update();
  }

  public update(): void{
      this.configurationService.getConfiguration().subscribe(value => this.configuration = value, err => this.errorMsg = err.message);
  }

  public save(): void{
      this.configurationService.updateConfiguration(this.configuration).subscribe(() => this.update(), err => this.errorMsg = err.message);
  }


}
