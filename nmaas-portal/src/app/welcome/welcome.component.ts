import { AppConfigService } from '../service/appconfig.service';
import {Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';

@Component({
  selector: 'app-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent implements OnInit {

  private height = 0;

  constructor(private appConfig: AppConfigService) { }

  ngOnInit() {
    this.onResize();
  }

  onResize() {
      this.height = document.getElementById("global-footer").offsetHeight + 10;
      //console.log(`Footer h: ${height}`);
      document.getElementById("welcome-container").style.marginBottom = `${this.height}px`;
      if(this.height > 75){
        document.getElementById("global-footer").style.textAlign = "center";
      }else{
        document.getElementById("global-footer").style.textAlign = "right";
      }
  }

  
}
