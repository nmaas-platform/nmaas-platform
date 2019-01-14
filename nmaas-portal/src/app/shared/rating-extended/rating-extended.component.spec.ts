import {async, ComponentFixture, getTestBed, TestBed} from '@angular/core/testing';

import { RatingExtendedComponent } from './rating-extended.component';
import {AppsService} from "../../service";
import {Observable} from "rxjs";
import {Rate} from "../../model";
import {HttpResponse} from "selenium-webdriver/http";
import {TooltipModule} from "ng2-tooltip-directive";
import {Pipe, PipeTransform} from "@angular/core";

class MockAppService{
  public getAppRateByUrl(urlPath: string): Observable<Rate> {
    return Observable.of(new Rate(1, 1, new Map()));
  }

  public setMyAppRateByUrl(urlPath: string): Observable<any> {
    return Observable.of(HttpResponse.prototype);
  }
}

@Pipe({
  name: "translate"
})
export class TranslatePipeMock implements PipeTransform {
  public name: string = "translate";

  public transform(query: string, ...args: any[]): any {
    return query;
  }
}

describe('RatingExtendedComponent', () => {
  let component: RatingExtendedComponent;
  let fixture: ComponentFixture<RatingExtendedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RatingExtendedComponent, TranslatePipeMock ],
      imports: [TooltipModule],
      providers: [
        {provide: AppsService, useClass: MockAppService},
      ]
    });
  TestBed.compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RatingExtendedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
