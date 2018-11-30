import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import { Router } from '@angular/router';
import {AppConfigService, ChangelogService} from "../../service";
import {ModalComponent} from "../modal";
import {ModalChangelogComponent} from "./modal-changelog/modal-changelog.component";
import {GitInfo} from "../../model/gitinfo";

@Component({
  selector: 'nmaas-footer',
  templateUrl: './footer.component.html',
  styleUrls: [ './footer.component.css' ]
})

export class FooterComponent implements OnInit {

  public gitInfo: GitInfo;

  @ViewChild(ModalChangelogComponent)
  private changeLog: ModalChangelogComponent;

  @ViewChild(ModalComponent)
  public modal:ModalComponent;

  constructor(private changelogService:ChangelogService, private router:Router, public appConfigService: AppConfigService) {
  }

  ngOnInit() {
    if(this.appConfigService.getShowGitInfo()){
        this.modal.setModalType("info");
        this.modal.setStatusOfIcons(true);
        this.changelogService.getGitInfo().subscribe(info => this.gitInfo = info);
    }
  }

  checkURL():boolean{
    return this.router.url === "/welcome/login" || this.router.url === "/welcome/registration";
  }

}