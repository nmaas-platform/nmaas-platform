import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AppStaticGlobalDeployParametersEditComponent } from './app-static-global-deploy-parameters-edit.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TooltipModule} from 'ng2-tooltip-directive';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {AppStorageVolume} from '../../../model/app-storage-volume';
import {AppAccessMethod} from '../../../model/app-access-method';

describe('AppStaticGlobalDeployParametersEditComponent', () => {
  let component: AppStaticGlobalDeployParametersEditComponent;
  let fixture: ComponentFixture<AppStaticGlobalDeployParametersEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppStaticGlobalDeployParametersEditComponent ],
      imports: [
        FormsModule,
          ReactiveFormsModule,
        TooltipModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader
          }
        })
      ]
    })
        .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppStaticGlobalDeployParametersEditComponent);
    component = fixture.componentInstance;
    component.appDeploymentSpec = {
      supportedDeploymentEnvironments: [],
      kubernetesTemplate: undefined,
      exposesWebUI: false,
      deployParameters: {},
      storageVolumes: [],
      accessMethods:  [],
      globalDeployParameters: {}
    }
    component.propertyName = 'deployParameters';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
