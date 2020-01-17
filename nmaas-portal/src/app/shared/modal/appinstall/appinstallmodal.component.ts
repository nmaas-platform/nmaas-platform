import {Component, OnInit, Input, ViewChild} from '@angular/core';
import {Router} from '@angular/router';

import {Application} from '../../../model/application';
import {ModalComponent} from '..';
import {AppInstanceService} from '../../../service/appinstance.service';
import {DomainService} from '../../../service/domain.service';
import { UserDataService } from '../../../service/userdata.service';
import { isUndefined } from 'util';

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
  selectedAppVersion: number;
  error:string;

  constructor(private appInstanceService: AppInstanceService, private domainService: DomainService, private userDataService: UserDataService, private router: Router) {
  }

  ngOnInit() {
    this.selectedAppVersion = this.app.appVersions.sort((a,b) => a.appVersionId - b.appVersionId)[0].appVersionId;
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
      this.appInstanceService.createAppInstance(this.domainId, this.selectedAppVersion, this.name).subscribe(
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
