import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppListComponent } from './applist.component';
import {RouterTestingModule} from "@angular/router/testing";
import {UserDataService} from "../../service/userdata.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {Observable, of} from "rxjs";
import {Component, Input} from "@angular/core";
import {AuthService} from "../../auth/auth.service";

@Component({
    selector: 'nmaas-applications-view',
    template: '<p>Mock application view</p>'
})
class AppViewMock{
    @Input()
    domainId: number;
    @Input()
    appView: any;
}

describe('ApplistComponent', () => {
  let component: AppListComponent;
  let fixture: ComponentFixture<AppListComponent>;
  let userDataService: UserDataService;

  beforeEach(async(() => {
      let mockAuthService = jasmine.createSpyObj('AuthService', ['getDomains']);
      mockAuthService.getDomains.and.returnValue(of([1]));
    TestBed.configureTestingModule({
      declarations: [
          AppListComponent,
          AppViewMock
      ],
        imports: [
            RouterTestingModule,
            HttpClientTestingModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useClass: TranslateFakeLoader
                }
            }),
        ],
        providers: [
            UserDataService,
            {provide: AuthService, useValue: mockAuthService}
        ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppListComponent);
    component = fixture.componentInstance;
    userDataService = fixture.debugElement.injector.get(UserDataService);
    spyOn(userDataService, 'selectDomainId').and.returnValue(0);
    fixture.detectChanges();
  });

  it('should create component', () => {
      expect(component).toBeDefined();
      let app = fixture.debugElement.componentInstance;
      expect(app).toBeTruthy();
  });

});
