import { Component, OnInit, ViewEncapsulation } from '@angular/core';

import { Application } from '../../model/application';
import { AppsService } from '../../service/apps.service';
import { TagService } from '../../service/tag.service';

export enum ListType {GRID, LIST}

@Component({
  selector: 'nmaas-applist',
  templateUrl: './applist.component.html',
  styleUrls: ['./applist.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [ AppsService, TagService ]
})
export class AppListComponent implements OnInit {

    private apps : Application[];
    private tags : string[];
    
    private selectedTag: string;
    
    private listType = ListType;
    private selectedListType: ListType;
    
    constructor(private appsService: AppsService, private tagService: TagService) { 
        this.selectedTag = 'all';
    }

    ngOnInit() {
        this.appsService.getApps().subscribe(applications => this.apps = applications);
        this.tagService.getTags().subscribe(tags => this.tags = tags);
        
        if(! this.selectedListType)
            this.selectedListType = ListType.GRID;
    }
    
}
