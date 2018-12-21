import {AuthService} from '../../auth/auth.service';
import {Component, OnInit, Input, ViewChild} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Location} from '@angular/common';
//import 'rxjs/add/operator/switchMap';

import {SecurePipe} from '../../pipe/index';
import {RateComponent} from '../../shared/rate/rate.component';
import {CommentsComponent} from '../../shared/comments/comments.component';
import {ScreenshotsComponent} from '../../shared/screenshots/screenshots.component';
import {AppsService, AppImagesService, AppInstanceService, AppConfigService} from '../../service/index';
import {Application} from '../../model/application';
import {Role} from '../../model/userrole';
import {AppSubscriptionsService} from '../../service/appsubscriptions.service';
import {UserDataService} from '../../service/userdata.service';
import {AppInstallModalComponent} from '../../shared/modal/appinstall/appinstallmodal.component';
import { Subject } from 'rxjs';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/isEmpty';
import {empty} from 'rxjs/observable/empty';
import {isUndefined} from 'util';
import {AppSubscription} from "../../model";

@Component({
  selector: 'nmaas-appdetails',
  templateUrl: './appdetails.component.html',
  styleUrls: ['../../../assets/css/main.css', './appdetails.component.css'],
  providers: [AppsService, RateComponent, CommentsComponent, AppImagesService, AppInstanceService, AppInstallModalComponent, SecurePipe]
})
export class AppDetailsComponent implements OnInit {

  protected state: number = 0;
  
  @ViewChild(AppInstallModalComponent)
  public readonly appInstallModal: AppInstallModalComponent;

  @ViewChild(CommentsComponent)
  public readonly comments: CommentsComponent;

  @ViewChild(RateComponent)
  public readonly appRate: RateComponent;

  public appId: number;
  public app: Application;
  public subscribed: boolean;
  public domainId: number;

  constructor(private appsService: AppsService,
    private appSubsService: AppSubscriptionsService,
    private appImagesService: AppImagesService,
    private appInstanceService: AppInstanceService,
    private userDataService: UserDataService,
    private appConfig: AppConfigService,
    private authService: AuthService,
    private router: Router, private route: ActivatedRoute, private location: Location) {
  }

  ngOnInit() {
       
    this.route.params.subscribe(params => {
      this.appId = +params['id'];
      this.appsService.getApp(this.appId).subscribe(application => this.app = application);
      this.userDataService.selectedDomainId.subscribe((domainId) => this.updateDomainSelection(domainId));
    });
  }

  public onRateChanged(): void {
    this.appRate.refresh();
  }

  protected updateDomainSelection(domainId: number): void {
    console.log('selected domainId:' + domainId);    
    this.domainId = domainId;

    if (isUndefined(this.appId)) {
      return;
    }

    let result: Observable<any> = null;
    if (isUndefined(domainId) || domainId === 0) {
      result = this.appSubsService.getAllByApplication(this.appId);
      result.isEmpty().subscribe(res => this.subscribed = !res, error => this.subscribed = false);
    } else {
      result = this.appSubsService.getSubscription(this.appId, domainId);
      result.subscribe((appSub:AppSubscription)=>this.subscribed=appSub.active, error=>this.subscribed = false);
    }
  }

  protected subscribe(): void {
    if (this.isSubscriptionAllowed()) {
      console.info('Subscribe appId=' + this.appId + ' to domainId=' + this.domainId);
      this.appSubsService.subscribe(this.domainId, this.appId).subscribe(() => this.subscribed = true);
    }
  }

  protected unsubscribe(): void {
    if (this.isSubscriptionAllowed()) {
      console.info('Unsubscribe appId=' + this.appId + ' from domainId=' + this.domainId);
      this.appSubsService.unsubscribe(this.domainId, this.appId).subscribe(() => this.subscribed = false);
    }
  }

  protected isSubscriptionAllowed(): boolean {
    if (isUndefined(this.domainId) || this.domainId === this.appConfig.getNmaasGlobalDomainId()) {
      return false;
    }

    if (this.authService.hasRole(Role[Role.ROLE_SYSTEM_ADMIN]) 
        || this.authService.hasDomainRole(this.domainId, Role[Role.ROLE_DOMAIN_ADMIN])) {
      return true;
    }

    return false;
  }

  protected isDeploymentAllowed(): boolean {
    if (isUndefined(this.domainId) || this.domainId === this.appConfig.getNmaasGlobalDomainId()) {
      return false;
    }

    if (this.authService.hasRole(Role[Role.ROLE_SYSTEM_ADMIN]) 
        || this.authService.hasDomainRole(this.domainId, Role[Role.ROLE_DOMAIN_ADMIN])
        || this.authService.hasDomainRole(this.domainId, Role[Role.ROLE_USER])) {
      return true;
    }

    return false;
  }

  protected refresh(): void {
    this.state += Math.random() * 123456;
  }
  
}
