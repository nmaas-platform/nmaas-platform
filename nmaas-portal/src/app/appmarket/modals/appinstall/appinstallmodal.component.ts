import {AuthService} from '../../../auth/auth.service';
import {Component, OnInit, Input, ViewChild} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';

import {Application} from '../../../model/application';
import {Domain} from '../../../model/domain';
import {ModalComponent} from '../../../shared/modal/index';
import {AppInstanceService} from '../../../service/appinstance.service';
import {DomainService} from '../../../service/domain.service';
import { UserDataService } from '../../../service/userdata.service';
import { isUndefined } from 'util';
import {Observable} from "rxjs/Observable";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'nmaas-modal-app-install',
  templateUrl: './appinstallmodal.component.html',
  styleUrls: ['./appinstallmodal.component.css'],
  providers: [AppInstanceService, DomainService, ModalComponent]
})
export class AppInstallModalComponent implements OnInit {

  @ViewChild(ModalComponent)
  public readonly modal: ModalComponent;

  @Input()
  app: Application;

  name: string;
  domainId: number;
  domainName: string;
  error:string;

  userDomains: Domain[];

  constructor(private appInstanceService: AppInstanceService, private domainService: DomainService, private userDataService: UserDataService, private router: Router, private translate:TranslateService) {
      const browserLang = translate.currentLang == null ? 'en' : translate.currentLang;
      translate.use(browserLang.match(/en|fr|pl/) ? browserLang : 'en');
  }

  ngOnInit() {
    this.userDataService.selectedDomainId.subscribe(domainId => 
      { 
        if(!(isUndefined(domainId) || domainId === 0)) {
          this.domainId = domainId;
          this.domainService.getOne(this.domainId).subscribe((domain) => this.domainName = domain.name);
        } else {
          this.modal.hide();
        }
      }
    );    
  }

  public create(): void {
    if(this.domainId && this.app && this.app.id) {
      this.appInstanceService.createAppInstance(this.domainId, this.app.id, this.name).subscribe(
        instanceId => {
          this.modal.hide();
          this.router.navigate(['/instances', instanceId.id]);
        },
          err=>{
          this.error = err.message;
        });
    }
  }

  public show(): void {
    this.modal.show();
  }

}
