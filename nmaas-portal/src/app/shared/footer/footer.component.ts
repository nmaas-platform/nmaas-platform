import {Component, OnInit} from '@angular/core';
import { Router } from '@angular/router';
import {AppConfigService, ChangelogService} from "../../service";
import {GitInfo} from "../../model/gitinfo";

@Component({
  selector: 'nmaas-footer',
  templateUrl: './footer.component.html',
  styleUrls: [ './footer.component.css' ]
})

export class FooterComponent implements OnInit {

  public gitInfo: GitInfo;

  constructor(private changelogService:ChangelogService, private router:Router, public appConfigService: AppConfigService) {
  }

  ngOnInit() {
    if(this.appConfigService.getShowGitInfo()){
        this.changelogService.getGitInfo().subscribe(info => this.gitInfo = info);
    }
  }

}