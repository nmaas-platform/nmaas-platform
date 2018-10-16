import {AfterContentChecked, AfterViewChecked, Component, OnInit, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'app-appmarket',
  templateUrl: './appmarket.component.html',
  styleUrls: [ '../../assets/css/main.css', './appmarket.component.css' ]
//  encapsulation: ViewEncapsulation.None
})
export class AppMarketComponent implements OnInit, AfterViewChecked, AfterContentChecked {

  private height = 0;
  private navHeight = 0;

  constructor() { }

  ngOnInit() {
      this.onResize();
  }

  ngAfterViewChecked(){
      this.onResize();
  }

  ngAfterContentChecked(){
      this.onResize();
  }

    onResize() {
        this.height = document.getElementById("global-footer").offsetHeight;
        this.navHeight = document.getElementById("navbar-appmarket").offsetHeight;
        document.getElementById("appmarket-container").style.marginBottom = `${this.height}px`;
        if(this.height > 90){
            document.getElementById("global-footer").style.textAlign = "center";
            document.getElementById("global-footer-version").style.lineHeight = `inherit`;
            document.getElementById("global-footer-version").style.paddingTop = '0';
        }else{
            document.getElementById("appmarket-container").style.paddingTop = `${this.navHeight + 10}px`;
            document.getElementById("global-footer").style.textAlign = "right";
            document.getElementById("global-footer-version").style.lineHeight = `${(Math.floor(this.height)/2)-8}px`;
            document.getElementById("global-footer-version").style.paddingTop = `${(Math.floor(this.height)/10)}px`
        }
    }
}
