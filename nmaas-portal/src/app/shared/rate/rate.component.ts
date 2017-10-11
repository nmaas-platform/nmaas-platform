import { Component, EventEmitter, OnInit, Input, Output, ViewEncapsulation, OnChanges, SimpleChanges } from '@angular/core';

import { AppsService } from '../../service/apps.service';
import { Rate } from '../../model/rate';

@Component({
  selector: 'rate',
  templateUrl: './rate.component.html',
  styleUrls: ['./rate.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [ AppsService ]
})
export class RateComponent implements OnInit, OnChanges {

    @Input()
    private pathUrl:string;
    
    @Input()
    editable: boolean = false;
    
    rate: Rate;
    
    @Output()
    onChange = new EventEmitter<boolean>()
    
    
  constructor(private appsService:AppsService) { }

  ngOnInit() {
      this.refresh();
  }

  ngOnChanges(changes: SimpleChanges) {
  	if (changes['pathUrl'])
  		this.refresh();
  }
  
  public refresh(): void {
  	this.appsService.getAppRateByUrl(this.pathUrl).subscribe(rate => this.rate = rate);
  }
  
  public update(rate: Number) {
  	if(this.editable) {
  		this.appsService.setMyAppRateByUrl(this.pathUrl + '/' + rate).subscribe( apiResponse => { 
  						this.refresh();
  						this.onChange.emit(true);
  					});
  	}
  }
  
}
