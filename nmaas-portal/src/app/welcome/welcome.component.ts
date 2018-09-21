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

  constructor(private appConfig: AppConfigService) { }

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
      console.log(`Footer h: ${this.height}`);
      document.getElementById("welcome-container").style.marginBottom = `${this.height + 15}px`;
      if(this.height > 90){
        document.getElementById("global-footer").style.textAlign = "center";
        document.getElementById("global-footer-version").style.lineHeight = `inherit`;
      }else{
        document.getElementById("global-footer").style.textAlign = "right";
        document.getElementById("global-footer-version").style.lineHeight = `${this.height-4}px`;
      }
  }

  
}
