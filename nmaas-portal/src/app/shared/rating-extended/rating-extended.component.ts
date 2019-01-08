import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Rate} from "../../model";
import {AppsService} from "../../service";

@Component({
  selector: 'rating-extended',
  templateUrl: './rating-extended.component.html',
  styleUrls: ['./rating-extended.component.css']
})
export class RatingExtendedComponent implements OnInit, OnChanges {

  @Input()
  private pathUrl:string;

  @Input()
  editable: boolean = false;

  rate: Rate;

  @Output()
  onChange = new EventEmitter<boolean>()

  public fiveStarPerc: number = 20;
  public fiveStarCount: number = 0;

  public fourStarPerc: number = 30;
  public fourStarCount: number = 0;

  public threeStarPerc: number = 0;
  public threeStarCount: number = 0;

  public twoStarPerc: number = 0;
  public twoStarCount: number = 0;

  public oneStarPerc: number = 0;
  public oneStarCount: number = 0;

  constructor(private appsService:AppsService) { }

  ngOnInit() {
    this.refresh();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['pathUrl'])
      this.refresh();
  }

  public setBreakdownsValues(){
    document.getElementById("five-star-progress").style.width = this.fiveStarPerc + "%";
    document.getElementById("four-star-progress").style.width = this.fourStarPerc + "%";
    document.getElementById("three-star-progress").style.width = this.threeStarPerc + "%";
    document.getElementById("two-star-progress").style.width = this.twoStarPerc + "%";
    document.getElementById("one-star-progress").style.width = this.oneStarPerc + "%";
  }

  public refresh(): void {
    this.appsService.getAppRateByUrl(this.pathUrl).subscribe(rate => this.rate = rate);
    this.setBreakdownsValues();
  }

  public update(rate: number) {
    if(this.editable) {
      this.appsService.setMyAppRateByUrl(this.pathUrl + '/' + rate).subscribe( apiResponse => {
        this.refresh();
        this.onChange.emit(true);
      });
    }
  }

}
