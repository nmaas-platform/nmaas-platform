import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UsersListComponent } from './userslist.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {HttpClientModule} from "@angular/common/http";
import {AppConfigService, DomainService, UserService} from "../../../service";
import {UserDataService} from "../../../service/userdata.service";
import {AuthService} from "../../../auth/auth.service";
import {FormsModule} from "@angular/forms";
import {JwtHelperService, JwtModule} from "@auth0/angular-jwt";

describe('UserslistComponent', () => {
  let component: UsersListComponent;
  let fixture: ComponentFixture<UsersListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UsersListComponent ],
      imports:[
          TranslateModule.forRoot({
              loader: {
                  provide: TranslateLoader,
                  useClass: TranslateFakeLoader
              },
          }),
          JwtModule.forRoot({
              config: {
                  tokenGetter: () => {
                      return '';
                  }
              }
          }),
          HttpClientModule,
          FormsModule
      ],
      providers:[ UserService, DomainService, UserDataService, AuthService, AppConfigService, JwtHelperService ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UsersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
      let app = fixture.debugElement.componentInstance;
      expect(app).toBeTruthy();
  });

});
