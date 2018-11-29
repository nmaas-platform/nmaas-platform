import {AfterContentChecked, AfterViewChecked, Component, OnInit} from '@angular/core';
import {AppConfigService} from "../../service";
import {isNullOrUndefined} from "util";
import {ContentDisplayService} from "../../service/content-display.service";
import {Content} from "../../model/content";

@Component({
  selector: 'app-privacy-policy-subpage',
  templateUrl: './privacy-policy-subpage.component.html',
  styleUrls: ['./privacy-policy-subpage.component.css'],
  providers: [ContentDisplayService]
})
export class PrivacyPolicySubpageComponent implements OnInit, AfterViewChecked, AfterContentChecked{

  private height = 0;

  public content: Content;

  constructor(private contentDisplayService: ContentDisplayService) {
  }

  ngOnInit() {
    this.onResize();
    this.getContent();
  }

  ngAfterContentChecked(){
    this.onResize();
  }

  ngAfterViewChecked(){
    this.onResize();
  }

  getContent(): void{
    this.contentDisplayService.getContent("pp").subscribe(content=> this.content = content);
  }

  onResize() {
    this.height = document.getElementById("global-footer").offsetHeight;
    //console.log(`Footer h: ${this.height}`);
    let navHeight = document.getElementById("navbar-welcome").offsetHeight;
    document.getElementById("welcome-container").style.marginBottom = `${this.height + 5}px`;
    document.getElementById("welcome-container").style.marginTop = `${navHeight + 2}px`;
    if(this.height > 90){
      document.getElementById("global-footer").style.textAlign = "center";
      document.getElementById("global-footer-version").style.lineHeight = `inherit`;
    }else{
      document.getElementById("global-footer").style.textAlign = "right";
      document.getElementById("global-footer-version").style.lineHeight = `${this.height-4}px`;
    }
  }

}