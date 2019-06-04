import {Component, OnInit} from '@angular/core';
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
  public gitLabConfig: GitLabConfig;

  constructor(private gitLabService: GitlabService, private route: ActivatedRoute, private router: Router) { super(); }

  ngOnInit() {
    this.gitLabService.getAll().subscribe(config => {
      if(config.length > 0) {
        this.gitLabConfig = config[0];
        this.router.navigate(['admin/gitlab/view'])
      } else{
        this.gitLabConfig = new GitLabConfig();
        this.mode = ComponentMode.CREATE;
      }
    });
  }

  public onDelete($event) {
    this.gitLabService.remove($event).subscribe(() => this.router.navigate(['/admin/gitlab']));
  }

  public onSave($event) {
    const newGitLabConfig = $event;

    if (!newGitLabConfig) {
      return;
    }

    if(this.isInMode(ComponentMode.CREATE)){
      this.gitLabService.add(newGitLabConfig)
          .subscribe(() => this.router.navigate(['/admin/gitlab/']));
    } else {
      this.gitLabService.update(newGitLabConfig)
          .subscribe(() => this.router.navigate(['/admin/gitlab']));
    }
  }

}
