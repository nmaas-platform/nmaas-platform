import {Component, OnInit, ViewEncapsulation, OnDestroy} from '@angular/core';

import {AppsService} from '../../service/apps.service';
import { AppSubscriptionsService } from '../../service/appsubscriptions.service';
import { UserDataService } from '../../service/userdata.service';
import { AppViewType } from '../../shared/common/viewtype';
import { Router, ActivatedRoute } from '@angular/router';
import {Location} from '@angular/common';
import { Subscription } from 'rxjs/Subscription';
import { isUndefined } from 'util';

@Component({
  selector: 'nmaas-applications',
  templateUrl: './applist.component.html',
  styleUrls: ['./applist.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [AppsService, AppSubscriptionsService]
})
export class AppListComponent implements OnInit, OnDestroy {

  public AppViewType = AppViewType;

  public appsView: AppViewType;
  public domainId: number;
  
  public selectedDomain: Subscription;
  
  constructor(protected userDataService: UserDataService,
              private router: Router,
              private route: ActivatedRoute, 
              private location: Location) {    
  }

  ngOnInit() {
    if(!isUndefined(this.route.snapshot.data.appViewType)) {
      this.appsView = AppViewType.APPLICATION;
    } else {
      this.appsView = this.route.snapshot.data.appView;
    }
    
    this.selectedDomain = this.userDataService.selectedDomainId.subscribe(domainId => this.domainId = domainId );
  }
  
  ngOnDestroy(): void {
    if(!isUndefined(this.selectedDomain)) {
      this.selectedDomain.unsubscribe();
    }
  }
  
  
//  filterAppsByName() {
//    const searchedAppName: string = this.searchedAppName;
//    if (searchedAppName.length > 0) {
//      this.filteredApps = this.apps.filter(app => app.name.toLocaleLowerCase().indexOf(searchedAppName) > -1);
//    } else {
//      this.filteredApps = this.apps.filter(app => true);
//    }
//  }
//
//  filterAppsByTag() {
//    const selectedTag: string = this.selectedTag;
//    if (selectedTag === 'all' || selectedTag === 'undefined') {
//      this.filteredApps = this.apps.filter(app => true);
//    } else {
//      this.filteredApps = this.apps.filter(app => app.tags.some(tag => tag === selectedTag));
//    }
//  }

}
