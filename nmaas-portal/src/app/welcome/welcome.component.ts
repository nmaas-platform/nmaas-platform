import { AppConfigService } from '../service/appconfig.service';
import {Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';

@Component({
  selector: 'app-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent implements OnInit {

  constructor(private appConfig: AppConfigService) { }

  ngOnInit() {
      this.onResize();
  }

  onResize(){
      let height = document.getElementById("global-footer").offsetHeight + 10;
      document.getElementById("welcome-container").style.marginBottom = `${height}px`;
  }
  
}
