import {Component, OnInit, ViewChild} from '@angular/core';
import { Router } from '@angular/router';
import {ChangelogService} from "../../service";
import {ModalComponent} from "../modal";

@Component({
  selector: 'nmaas-footer',
  templateUrl: './footer.component.html',
  styleUrls: [ './footer.component.css' ]
})
export class FooterComponent implements OnInit {

  changelog:any;

  @ViewChild(ModalComponent)
  private modal:ModalComponent;

  constructor(private changelogService:ChangelogService, private router:Router) { }

  ngOnInit() {
    this.changelogService.getChangelog().subscribe(changelog => this.changelog = changelog);
  }

  showChangelog(){
    if(this.checkURL()){
      this.modal.show();
    } else{
      this.router.navigate(['/changelog']);
    }
  }

  checkURL():boolean{
    return this.router.url === "/welcome/login" || this.router.url === "/welcome/registration";
  }

}