import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationsViewComponent } from './applications.component';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule} from '@angular/forms';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {AppConfigService, AppInstanceService, AppsService, DomainService, TagService} from '../../service';
import {AppSubscriptionsService} from '../../service/appsubscriptions.service';
import {UserDataService} from '../../service/userdata.service';
import {SearchComponent} from '../common/search/search.component';
import {AppListComponent} from './list/applist.component';
import {AppElementComponent} from './list/element/appelement.component';
import {RateComponent} from '../rate';
import {RouterTestingModule} from '@angular/router/testing';
import {SecurePipe} from '../../pipe';
import {TagFilterComponent} from '../common/tagfilter/tagfilter.component';
import {of} from 'rxjs';
import {AppInstallModalComponent} from '../modal/appinstall';
import {ModalComponent} from '../modal';
import {TooltipModule} from 'ng2-tooltip-directive';
import {Domain} from '../../model/domain';

describe('ApplicationsComponent', () => {
  let component: ApplicationsViewComponent;
  let fixture: ComponentFixture<ApplicationsViewComponent>;
  let appsService: AppsService;
  let appSubscriptionsService: AppSubscriptionsService;
  let userDataService: UserDataService;
  let tagService: TagService;
  let instanceService: AppInstanceService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
          AppInstallModalComponent,
          ModalComponent,
          SearchComponent,
          AppListComponent,
          AppElementComponent,
          ApplicationsViewComponent,
          RateComponent,
          SecurePipe,
          TagFilterComponent
      ],
      imports: [
          FormsModule,
          RouterTestingModule,
          HttpClientModule,
          TranslateModule.forRoot({
              loader: {
                  provide: TranslateLoader,
                  useClass: TranslateFakeLoader
              }
          }),
          TooltipModule
      ],
      providers: [AppsService, AppSubscriptionsService, UserDataService, AppConfigService, TagService, DomainService, AppInstanceService]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationsViewComponent);
    component = fixture.componentInstance;
    component.domainId = 1;
    component.domain = of<Domain>();
    appsService = fixture.debugElement.injector.get(AppsService);
    appSubscriptionsService = fixture.debugElement.injector.get(AppSubscriptionsService);
    userDataService = fixture.debugElement.injector.get(UserDataService);
    tagService = fixture.debugElement.injector.get(TagService);
    instanceService = fixture.debugElement.injector.get(AppInstanceService);
    spyOn(tagService, 'getTags').and.returnValue(of([]));
    spyOn(instanceService, 'getStatistics').and.returnValue(of({}))
    fixture.detectChanges();
  });

  it('should create', () => {
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });

});
