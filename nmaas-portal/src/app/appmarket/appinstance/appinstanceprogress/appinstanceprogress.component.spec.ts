/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import {DebugElement, Pipe, PipeTransform} from '@angular/core';

import { AppInstanceProgressComponent } from './appinstanceprogress.component';
import {AppsService} from "../../../service";
import {TranslateService} from "@ngx-translate/core";

@Pipe({
  name: "translate"
})
class TranslatePipeMock implements PipeTransform {
  public name: string = "translate";

  public transform(query: string, ...args: any[]): any {
    return query;
  }
}

class MockTranslateService{
  public instant(key: string): string{
    return "";
  }
}

describe('AppInstanceProgressComponent', () => {
  let component: AppInstanceProgressComponent;
  let fixture: ComponentFixture<AppInstanceProgressComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppInstanceProgressComponent, TranslatePipeMock ],
      providers: [
        {provide: TranslateService, useClass: MockTranslateService},
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppInstanceProgressComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
