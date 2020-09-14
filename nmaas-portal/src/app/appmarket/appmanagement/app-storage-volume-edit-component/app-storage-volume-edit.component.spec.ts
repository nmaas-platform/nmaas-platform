import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AppStorageVolumeEditComponent} from './app-storage-volume-edit.component';
import {FormsModule} from '@angular/forms';
import {TooltipModule} from 'ng2-tooltip-directive';
import {ServiceStorageVolumeType} from '../../../model/service-storage-volume';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';

describe('AppStorageVolumeEditComponent', () => {
  let component: AppStorageVolumeEditComponent;
  let fixture: ComponentFixture<AppStorageVolumeEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppStorageVolumeEditComponent ],
      imports: [
        FormsModule,
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
    fixture = TestBed.createComponent(AppStorageVolumeEditComponent);
    component = fixture.componentInstance;
    component.id = 0;
    component.storageVolumeTypes = ['MAIN', 'SHARED'];
    component.storageVolume = {
      id: 21,
      type: ServiceStorageVolumeType.MAIN,
      defaultStorageSpace: 3,
      deployParameters: {}
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
