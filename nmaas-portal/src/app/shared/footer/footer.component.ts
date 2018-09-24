import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import { Router } from '@angular/router';
import {ChangelogService} from "../../service";
import {ModalComponent} from "../modal";
import {ModalChangelogComponent} from "./modal-changelog/modal-changelog.component";

@Component({
  selector: 'nmaas-footer',
  templateUrl: './footer.component.html',
  styleUrls: [ './footer.component.css' ]
})
export class FooterComponent implements OnInit {

  gitInfo:string[];

  @ViewChild(ModalChangelogComponent)
  private changeLog: ModalChangelogComponent;

  @ViewChild(ModalComponent)
  private modal:ModalComponent;

  constructor(private changelogService:ChangelogService, private router:Router) { }

  ngOnInit() {
    this.changelogService.getGitInfo().subscribe(info => this.gitInfo = info);
  }

  checkURL():boolean{
    return this.router.url === "/welcome/login" || this.router.url === "/welcome/registration";
  }

}