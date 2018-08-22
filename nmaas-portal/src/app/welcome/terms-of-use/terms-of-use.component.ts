import { Component, OnInit } from '@angular/core';
import {BaseComponent} from "../../shared/common/basecomponent/base.component";
import {ContentDisplayService} from "../../service/content-display.service";
import {Content} from "../../model/content";

@Component({
  selector: 'app-terms-of-use',
  templateUrl: './terms-of-use.component.html',
  styleUrls: ['./terms-of-use.component.css']
})
export class TermsOfUseComponent extends BaseComponent implements OnInit {

  constructor(protected contentDisplayService: ContentDisplayService) {super();}

  public content: Content;

  getContent(): void{
    this.contentDisplayService.getContent("tos".toString()).subscribe(content=> this.content = content);
  }

  ngOnInit() {
    this.getContent()
  }

}


/*
  constructor(protected profileService:ProfileService) {super()}

  public user:User;

  ngOnInit() {
    this.profileService.getOne().subscribe((user)=>this.user = user)
  }
 */