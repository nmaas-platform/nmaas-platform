import {Component, OnInit, ViewEncapsulation, Input, ViewChild} from '@angular/core';
import {Application} from '../../../../model';
import {AppImagesService} from '../../../../service';
import {RateComponent} from '../../../rate';
import {DefaultLogo} from '../../../../directive/defaultlogo.directive';

import {isUndefined} from 'util';
import {SecurePipe} from '../../../../pipe';
import {Router} from '@angular/router';
import {AppInstallModalComponent} from '../../../modal/appinstall';
import {AppConfigService} from '../../../../service';
import {AuthService} from '../../../../auth/auth.service';
import {TranslateService} from '@ngx-translate/core';
import {AppDescription} from '../../../../model/appdescription';
import {Domain} from '../../../../model/domain';

@Component({
  selector: 'nmaas-applist-element',
  providers: [DefaultLogo, RateComponent, AppImagesService, SecurePipe, AppInstallModalComponent],
  templateUrl: './appelement.component.html',
  styleUrls: ['./appelement.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class AppElementComponent implements OnInit {

  @Input()
  public app: Application;

  @Input()
  public selected: boolean;

  @Input()
  public domainId: number;

  @Input()
  public domain: Domain;

  @ViewChild(AppInstallModalComponent)
  public readonly modal: AppInstallModalComponent;

  constructor(public appImagesService: AppImagesService, public appConfigService: AppConfigService, public router: Router,
              public authService: AuthService, public translate: TranslateService) {
  }

  ngOnInit() {
    if (isUndefined(this.selected)) {
      this.selected = false;
    }
  }

  public showDeployButton(): boolean {
    return this.domainId !== this.appConfigService.getNmaasGlobalDomainId() &&
        !this.authService.hasDomainRole(this.domainId, 'ROLE_GUEST');
  }

  public getDescription(): AppDescription {
    return this.app.descriptions.find(val => val.language === this.translate.currentLang);
  }

  public isApplicationEnabledInDomain(): boolean {
    const appId = this.app.id;
    return this.domain.applicationStatePerDomain.find(value => value.applicationBaseId === appId).enabled || false;
  }
}
