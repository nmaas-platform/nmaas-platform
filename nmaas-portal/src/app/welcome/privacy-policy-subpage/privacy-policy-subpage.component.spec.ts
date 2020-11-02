import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PrivacyPolicySubpageComponent } from './privacy-policy-subpage.component';
import {ContentDisplayService} from '../../service/content-display.service';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {RouterTestingModule} from '@angular/router/testing';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';

describe('PrivacyPolicySubpageComponent', () => {
  let component: PrivacyPolicySubpageComponent;
  let fixture: ComponentFixture<PrivacyPolicySubpageComponent>;

  beforeEach(async(() => {
    const contentDisplayServiceSpy = createSpyObj('ContentDisplayService', ['getContent'])
    contentDisplayServiceSpy.getContent.and.returnValue(of({}))

    TestBed.configureTestingModule({
      declarations: [ PrivacyPolicySubpageComponent ],
      imports: [
          RouterTestingModule,
          TranslateModule.forRoot({
              loader: {
                  provide: TranslateLoader,
                  useClass: TranslateFakeLoader
              }
          })
      ],
      providers: [
        {provide: ContentDisplayService, useValue: contentDisplayServiceSpy}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PrivacyPolicySubpageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // TODO mock 'document' object properly
  // it('should create', () => {
  //  expect(component).toBeTruthy();
  // });
});
