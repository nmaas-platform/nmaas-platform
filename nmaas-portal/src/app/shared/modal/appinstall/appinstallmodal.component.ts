import {Component, OnInit, Input, ViewChild} from '@angular/core';
import {Router} from '@angular/router';

import {Application} from '../../../model';
import {ModalComponent} from '..';
import {AppInstanceService, DomainService} from '../../../service';
import { UserDataService } from '../../../service/userdata.service';
import { isUndefined } from 'util';
import {Domain} from '../../../model/domain';

@Component({
  selector: 'nmaas-modal-app-install',
  templateUrl: './appinstallmodal.component.html',
  styleUrls: ['./appinstallmodal.component.css'],
  providers: [AppInstanceService, DomainService, ModalComponent]
})
export class AppInstallModalComponent implements OnInit {

  @ViewChild(ModalComponent, { static: true })
  public readonly modal: ModalComponent;

  @Input()
  app: Application;

  @Input()
  domain: Domain;

  name: string;
  domainId: number;
  domainName: string;
  selectedAppVersion: number;
  error: string;

  constructor(private appInstanceService: AppInstanceService,
              private domainService: DomainService,
              private userDataService: UserDataService,
              private router: Router) {
  }

  ngOnInit() {
    this.app.appVersions.sort((a, b) => a.appVersionId - b.appVersionId);
    this.selectedAppVersion = this.app.appVersions[0].appVersionId;
    this.domainId = this.domain.id;
    this.domainName = this.domain.name;
  }

  public create(): void {
    if (this.domainId && this.app && this.app.id) {
      this.appInstanceService.createAppInstance(this.domainId, this.selectedAppVersion, this.name).subscribe(
        instanceId => {
          this.modal.hide();
          this.router.navigate(['/instances', instanceId.id]);
        },
          err => {
          this.error = err.message;
        });
    }
  }

  public show(): void {
    this.modal.show();
  }

}
