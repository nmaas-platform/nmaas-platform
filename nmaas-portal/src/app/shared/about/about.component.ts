import { Component, OnInit } from '@angular/core';
import {AppConfigService, ChangelogService} from "../../service";
import {GitInfo} from "../../model/gitinfo";

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent implements OnInit {

  public gitInfo: GitInfo;

  constructor(private changelogService:ChangelogService, private appConfigService:AppConfigService) {
    if(this.appConfigService.getShowGitInfo()){
      this.changelogService.getGitInfo().subscribe(info => this.gitInfo = info);
    }
  }

  ngOnInit() {
  }

}
