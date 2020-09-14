import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LanguageDetailsComponent } from './languagedetails.component';
import {TranslateModule} from '@ngx-translate/core';
import {InputSwitchModule} from 'primeng/inputswitch';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {InternationalizationService} from '../../../../service/internationalization.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {AppConfigService} from '../../../../service';
import {RouterTestingModule} from '@angular/router/testing';

describe('LanguageDetailsComponent', () => {
  let component: LanguageDetailsComponent;
  let fixture: ComponentFixture<LanguageDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), InputSwitchModule, FormsModule, CommonModule, HttpClientTestingModule, RouterTestingModule],
      declarations: [ LanguageDetailsComponent ],
      providers: [InternationalizationService, AppConfigService]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LanguageDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
