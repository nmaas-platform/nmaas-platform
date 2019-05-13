import {AuthService} from '../../auth/auth.service';
import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {SecurePipe} from '../../pipe/index';
import {RateComponent} from '../../shared/rate/rate.component';
import {CommentsComponent} from '../../shared/comments/comments.component';
import {AppConfigService, AppImagesService, AppInstanceService, AppsService} from '../../service/index';
import {Application} from '../../model/application';
import {Role} from '../../model/userrole';
import {AppSubscriptionsService} from '../../service/appsubscriptions.service';
import {UserDataService} from '../../service/userdata.service';
import {AppInstallModalComponent} from '../../shared/modal/appinstall/appinstallmodal.component';
import {Observable} from 'rxjs';
import {isNullOrUndefined, isUndefined} from 'util';
import {AppSubscription} from "../../model";
import {AppDescription} from "../../model/appdescription";
import {TranslateService} from "@ngx-translate/core";
import {ApplicationState} from "../../model/applicationstate";

//import 'rxjs/add/operator/switchMap';

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
    private translate:TranslateService,
    private router: Router, private route: ActivatedRoute, private location: Location) {
  }

  ngOnInit() {
       
    this.route.params.subscribe(params => {
      this.appId = +params['id'];
      this.appsService.getBaseApp(this.appId).subscribe(application => this.app = application);
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
    if (isUndefined(domainId) || domainId === 0 || this.appConfig.getNmaasGlobalDomainId() === domainId) {
      result = this.appSubsService.getAllByApplication(this.appId);
      result.subscribe(() => this.subscribed=false);
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

  public isSubscriptionAllowed(): boolean {
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

  public getDescription(): AppDescription {
    if(isNullOrUndefined(this.app)){
      return;
    }
    return this.app.descriptions.find(val => val.language == this.translate.currentLang);
  }

  public getPathUrl(id: number): string{
    if(!isNullOrUndefined(id) && !isNaN(id)){
      return '/apps/' + id + '/rate/my';
    }else{
      return "";
    }
  }

  public isActive(state: any): boolean {
    return this.getStateAsString(state) === ApplicationState[ApplicationState.ACTIVE];
  }

  public isDisabled(state: any): boolean {
    return this.getStateAsString(state) === ApplicationState[ApplicationState.DISABLED];
  }

  public getStateAsString(state: any): string {
    return typeof state === "string" && isNaN(Number(state.toString())) ? state: ApplicationState[state];
  }

  public getValidLink(url: string) : string {
    if(isNullOrUndefined(url)){
      return;
    }
    if(!url.startsWith("http://") && !url.startsWith("https://")){
      return '//' + url;
    }
    return url;
  }

}
