import { async, ComponentFixture, TestBed } from '@angular/core/testing';

//import { ShibbolethDetailsComponent } from './shibboleth-details.component';
import {ShibbolethDetailsComponent} from "../../../../shared/admin/shibboleth/details/shibboleth-details.component";
import {FormsModule} from "@angular/forms";
import {RouterTestingModule} from "@angular/router/testing";
import {HttpClient, HttpHandler} from "@angular/common/http";
import {AppConfigService} from "../../../../service";

describe('ShibbolethDetailsComponent', () => {
  let component: ShibbolethDetailsComponent;
  let fixture: ComponentFixture<ShibbolethDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ShibbolethDetailsComponent ],
        imports: [
            FormsModule,
            RouterTestingModule,
        ],
        providers: [
            HttpClient,
            HttpHandler,
            AppConfigService,
        ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ShibbolethDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
