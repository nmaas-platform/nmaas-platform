import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {BaseComponent} from "../../../common/basecomponent/base.component";
import {GitLabConfig} from "../../../../model/gitlab";
import {ComponentMode} from "../../../common/componentmode";
import {Router} from "@angular/router";

@Component({
  selector: 'nmaas-gitlabdetails',
  templateUrl: './gitlab-details.component.html',
  styleUrls: ['./gitlab-details.component.css']
})
export class GitlabDetailsComponent extends BaseComponent implements OnInit {

  @Input()
  gitLabConfig: GitLabConfig = new GitLabConfig();

  @Output()
  onSave: EventEmitter<GitLabConfig> = new EventEmitter<GitLabConfig>();

  @Output()
  onDelete: EventEmitter<number> = new EventEmitter<number>();

  constructor(private router: Router) {
    super();
  }

  ngOnInit() {
  }

  public onModeChange(): void {
      const newMode: ComponentMode = (this.mode === ComponentMode.VIEW ? ComponentMode.EDIT : ComponentMode.VIEW);
      if (this.isModeAllowed(newMode)) {
          this.mode = newMode;
          if(this.mode === ComponentMode.VIEW){
            this.router.navigate(['admin/gitlab'])
          }
      }
  }

  public submit(){
    this.onSave.emit(this.gitLabConfig);
  }

  public remove(){
    this.onDelete.emit(this.gitLabConfig.id);
  }

}
