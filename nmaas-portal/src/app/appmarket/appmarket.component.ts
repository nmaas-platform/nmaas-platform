import {AfterViewChecked, Component, OnInit, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'app-appmarket',
  templateUrl: './appmarket.component.html',
  styleUrls: [ '../../assets/css/main.css', './appmarket.component.css' ]
//  encapsulation: ViewEncapsulation.None
})
export class AppMarketComponent implements OnInit, AfterViewChecked {

  private height = 0;

  constructor() { }

  ngOnInit() {
      this.onResize();
  }

    ngAfterViewChecked(){
        this.onResize();
    }

    onResize() {
        this.height = document.getElementById("global-footer").offsetHeight;
        console.log(`Footer h: ${this.height}`);
        document.getElementById("appmarket-container").style.marginBottom = `${this.height}px`;
        if(this.height > 60){
            document.getElementById("global-footer").style.textAlign = "center";
        }else{
            document.getElementById("global-footer").style.textAlign = "right";
        }
    }
}
