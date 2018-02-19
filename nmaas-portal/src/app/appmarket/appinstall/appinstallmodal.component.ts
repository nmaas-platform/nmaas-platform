import {AuthService} from '../../auth/auth.service';
import {Component, OnInit, Input, ViewChild} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';

import {Application} from '../../model/application';
import {Domain} from '../../model/domain';
import {ModalComponent} from '../../shared/modal/index';
import {AppInstanceService} from '../../service/appinstance.service';
import {DomainService} from '../../service/domain.service';

@Component({
  selector: 'nmaas-appInstallModal',
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

  userDomains: Domain[];

  constructor(private appInstanceService: AppInstanceService, private domainService: DomainService, private router: Router) {
    this.domainService.getMyDomains().subscribe(
      (domains: Domain[]) =>
        this.userDomains = domains.filter(domain => domain.id !== domainService.getGlobalDomainId())
    );
  }

  ngOnInit() {
  }

  public create(): void {
    this.appInstanceService.createAppInstance(this.domainId, this.app.id, this.name).subscribe(
      instanceId => {
        this.modal.hide();
        this.router.navigate(['/domains/', this.domainId, '/instances', instanceId.id]);
      });
  }

  public show(): void {
    this.modal.show();
  }

}
