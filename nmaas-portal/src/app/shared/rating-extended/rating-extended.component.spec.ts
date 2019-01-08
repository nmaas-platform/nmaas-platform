import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RatingExtendedComponent } from './rating-extended.component';
import {AppsService} from "../../service";
import {Observable} from "rxjs";
import {Rate} from "../../model";
import {HttpResponse} from "selenium-webdriver/http";

class MockAppService{
  public getAppRateByUrl(urlPath: string): Observable<Rate> {
    return Observable.of(new Rate(1));
  }

  public setMyAppRateByUrl(urlPath: string): Observable<any> {
    return Observable.of(HttpResponse.prototype);
  }
}

describe('RatingExtendedComponent', () => {
  let component: RatingExtendedComponent;
  let fixture: ComponentFixture<RatingExtendedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RatingExtendedComponent ],
      providers: [ {provide: AppsService, useClass: MockAppService} ]
    })
    .compileComponents();

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
