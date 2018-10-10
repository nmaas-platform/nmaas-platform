import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ConfigurationDetailsComponent} from './configurationdetails.component';
import {FormsModule} from "@angular/forms";
import {RouterTestingModule} from "@angular/router/testing";
import {AppConfigService, ConfigurationService} from "../../../../service";
import {HttpClient, HttpHandler} from "@angular/common/http";
import {AppComponent} from "../../../../app.component";
import {Observable} from "rxjs";
import {Configuration} from "../../../../model/configuration";
import {BaseComponent} from "../../../../shared/common/basecomponent/base.component";

class MockConfigurationService{
    protected uri:string;

    constructor() {
        this.uri = 'http://localhost/api';
    }

    public getConfiguration():Observable<Configuration>{
        return Observable.of<Configuration>();
    }

    public updateConfiguration(configuration:Configuration):Observable<any>{
        return Observable.of<Configuration>();
    }
}

describe('ConfigurationDetailsComponent', () => {
  let component: ConfigurationDetailsComponent;
  let fixture: ComponentFixture<ConfigurationDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigurationDetailsComponent ],
        imports: [
            FormsModule,
            RouterTestingModule,
        ],
        providers: [
            {provide: ConfigurationService, useClass: MockConfigurationService},
            HttpClient,
            HttpHandler,
            AppConfigService,
            AppComponent,
            BaseComponent
        ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigurationDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the app', () => {
      let app = fixture.debugElement.componentInstance;
      expect(app).toBeTruthy();
  });
});
