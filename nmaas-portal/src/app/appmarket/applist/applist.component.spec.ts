import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppListComponent } from './applist.component';
import { AppListComponent as AppSharedListComponent} from "../../shared/applications/list/applist.component";
import {RouterTestingModule} from "@angular/router/testing";
import {AppSubscriptionsService} from "../../service/appsubscriptions.service";
import {UserDataService} from "../../service/userdata.service";
import {ApplicationsViewComponent} from "../../shared/applications/applications.component";
import {AppConfigService, AppsService, DomainService, TagService} from '../../service';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {SearchComponent} from "../../shared/common/search/search.component";
import {TagFilterComponent} from "../../shared/common/tagfilter/tagfilter.component";
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AppElementComponent} from "../../shared/applications/list/element/appelement.component";
import {RateComponent} from "../../shared/rate";
import {PipesModule} from "../../pipe/pipes.module";
import {AppInstallModalComponent} from "../../shared/modal/appinstall";
import {ModalComponent} from "../../shared/modal";
import {Observable, of} from "rxjs";
import {TooltipModule} from 'ng2-tooltip-directive';

describe('ApplistComponent', () => {
  let component: AppListComponent;
  let fixture: ComponentFixture<AppListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppListComponent, ApplicationsViewComponent, SearchComponent, AppInstallModalComponent, ModalComponent,
          AppSharedListComponent, TagFilterComponent, AppElementComponent, RateComponent],
        imports: [
            RouterTestingModule,
            PipesModule,
            HttpClientTestingModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useClass: TranslateFakeLoader
                }
            }),
            FormsModule,
            ReactiveFormsModule,
            TooltipModule
        ],
        providers: [
            AppConfigService,
            AppSubscriptionsService,
            UserDataService,
            AppsService,
            TagService,
            DomainService
        ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppListComponent);
    component = fixture.componentInstance;
    spyOn(fixture.debugElement.injector.get(TagService), 'getTags').and.returnValue(of([]));
    fixture.detectChanges();
  });

  it('should create component', () => {
      let app = fixture.debugElement.componentInstance;
      expect(app).toBeTruthy();
  });

});
