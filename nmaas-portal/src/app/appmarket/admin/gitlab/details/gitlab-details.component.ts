import { Component, OnInit } from '@angular/core';
import {BaseComponent} from "../../../../shared/common/basecomponent/base.component";
import {GitLabConfig} from "../../../../model/gitlab";
import {GitlabService} from "../../../../service/gitlab.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ComponentMode} from "../../../../shared";

@Component({
  selector: 'app-gitlabdetails',
  templateUrl: './gitlab-details.component.html',
  styleUrls: ['./gitlab-details.component.css']
})
export class GitlabDetailsComponent extends BaseComponent implements OnInit {
  private config_id:number;
  private gitLabConfig:GitLabConfig;

  constructor(private gitLabService:GitlabService, private route:ActivatedRoute, private router:Router) { super();}

  ngOnInit() {
    this.gitLabService.getAll().subscribe(config =>{
      if(config.length > 0){
        this.gitLabConfig = config[0];
        this.config_id = this.gitLabConfig.id;
        this.router.navigate(['admin/gitlab',this.config_id])
      } else{
        this.gitLabConfig = new GitLabConfig();
        this.mode = ComponentMode.CREATE;
      }
    });
  }

  public onDelete($event){
    this.gitLabService.remove($event).subscribe((response) => this.router.navigate(['/admin/gitlab']));
  }

  public onSave($event){
    const newGitLabConfig = $event;

    if(!newGitLabConfig){
      return;
    }
    if(newGitLabConfig.id){
      this.gitLabService.update(newGitLabConfig)
          .subscribe(e=>this.router.navigate(['/admin/gitlab']));
    } else{
      newGitLabConfig.id = this.config_id;
      this.gitLabService.add(newGitLabConfig)
          .subscribe(id=>this.router.navigate(['/admin/gitlab/', id]));
    }
  }

}
