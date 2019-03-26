import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LanguageListComponent } from './languagelist.component';
import {TranslateModule} from "@ngx-translate/core";
import {FormsModule} from "@angular/forms";
import {RouterTestingModule} from "@angular/router/testing";
import {SharedModule} from "../../../../shared";
import {InternationalizationService} from "../../../../service/internationalization.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AppConfigService} from "../../../../service";

describe('LanguagelistComponent', () => {
  let component: LanguageListComponent;
  let fixture: ComponentFixture<LanguageListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), FormsModule, RouterTestingModule, SharedModule, HttpClientTestingModule],
      declarations: [ LanguageListComponent ],
      providers: [InternationalizationService, AppConfigService]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LanguageListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

});
