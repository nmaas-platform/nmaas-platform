import { AppConfigService } from '../service/appconfig.service';
import {
    AfterContentChecked,
    AfterViewChecked,
    AfterViewInit,
    Component,
    ElementRef,
    HostListener,
    OnInit,
    ViewChild
} from '@angular/core';

@Component({
  selector: 'app-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent implements OnInit, AfterViewChecked, AfterContentChecked{

  private height = 0;

  constructor(private appConfig: AppConfigService){
  }

  ngOnInit() {
    this.onResize();
  }

  ngAfterContentChecked(){
      this.onResize();
  }

  ngAfterViewChecked(){
      this.onResize();
  }

  onResize() {
      this.height = document.getElementById("global-footer").offsetHeight;
      //console.log(`Footer h: ${this.height}`);
      let navHeight = document.getElementById("navbar-welcome").offsetHeight;
      document.getElementById("welcome-container").style.marginBottom = `${this.height + 5}px`;
      document.getElementById("welcome-container").style.marginTop = `${navHeight + 2}px`;
      document.getElementById("login-out").style.maxHeight = `calc(95vh - ${this.height +  navHeight + 10}px)`;
      document.getElementById("login-out").style.paddingTop = `${navHeight}`;
      if(this.height > 90){
        document.getElementById("global-footer").style.textAlign = "center";
        document.getElementById("login-out").style.maxHeight = `calc(94vh - ${this.height +  navHeight + 10}px)`;
      }else{
        document.getElementById("global-footer").style.textAlign = "right";
      }
  }
}
