import {Component, OnInit, Input, ViewChild} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Location} from '@angular/common';
//import 'rxjs/add/operator/switchMap';

import {SecurePipe} from '../../pipe/index';
import {RateComponent} from '../../shared/rate/rate.component';
import {CommentsComponent} from '../../shared/comments/comments.component';
import {ScreenshotsComponent} from '../../shared/screenshots/screenshots.component';
import {AppsService, AppImagesService, AppInstanceService} from '../../service/index';
import {Application} from '../../model/application';
import {AppInstallModalComponent} from '../appinstall/appinstallmodal.component';

@Component({
  selector: 'nmaas-appdetails',
  templateUrl: './appdetails.component.html',
  styleUrls: ['../../../assets/css/main.css', './appdetails.component.css'],
  providers: [AppsService, RateComponent, CommentsComponent, AppImagesService, AppInstanceService, AppInstallModalComponent, SecurePipe]
})
export class AppDetailsComponent implements OnInit {

  @ViewChild(AppInstallModalComponent)
  public readonly appInstallModal: AppInstallModalComponent;

  @ViewChild(CommentsComponent)
  public readonly comments: CommentsComponent;

  @ViewChild(RateComponent)
  public readonly appRate: RateComponent;

  app: Application;

  private id: Number;

  constructor(private appsService: AppsService, private appImagesService: AppImagesService,
              private appInstanceService: AppInstanceService, private router: Router,
              private route: ActivatedRoute, private location: Location) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.id = +params['id'];
      this.appsService.getApp(this.id).subscribe(application => this.app = application);
    });
  }

  public onRateChanged(): void {
    this.appRate.refresh();
  }
}
