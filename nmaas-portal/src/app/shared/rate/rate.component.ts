import { Component, OnInit, Input, ViewEncapsulation } from '@angular/core';

import { AppsService } from '../../service/apps.service';
import { Rate } from '../../model/rate';

@Component({
  selector: 'rate',
  templateUrl: './rate.component.html',
  styleUrls: ['./rate.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [ AppsService ]
})
export class RateComponent implements OnInit {

    @Input()
    private pathUrl:string;
    
    rate: Rate;
    
  constructor(private appsService:AppsService) { }

  ngOnInit() {
      this.appsService.getAppRateByUrl(this.pathUrl).subscribe(rate => this.rate = rate);
  }

}
