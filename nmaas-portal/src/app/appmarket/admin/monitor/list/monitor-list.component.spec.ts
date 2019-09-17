import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {MonitorListComponent} from './monitor-list.component';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {MonitorService} from "../../../../service/monitor.service";
import {RouterTestingModule} from "@angular/router/testing";
import {HttpClientModule} from "@angular/common/http";
import {AppConfigService} from "../../../../service";
import {Observable, of} from "rxjs";
import {TimeFormat} from "../../../../model/monitorentry";

describe('MonitorListComponent', () => {
  let component: MonitorListComponent;
  let fixture: ComponentFixture<MonitorListComponent>;
  let monitorService: MonitorService;
  let appConfigService: AppConfigService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MonitorListComponent ],
      imports: [
          RouterTestingModule,
          HttpClientModule,
          TranslateModule.forRoot({
              loader: {
                  provide: TranslateLoader,
                  useClass: TranslateFakeLoader
              }
          })
      ],
      providers: [MonitorService, AppConfigService]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MonitorListComponent);
    appConfigService = fixture.debugElement.injector.get(AppConfigService);
    component = fixture.componentInstance;
    monitorService = fixture.debugElement.injector.get(MonitorService);
    spyOn(monitorService, 'getAllMonitorEntries').and.returnValue(of([]));
    fixture.detectChanges();
  });

  it('should create component', ()=>{
    let app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should update list of entries', ()=>{
      component.ngOnInit();
      expect(monitorService.getAllMonitorEntries).toHaveBeenCalledTimes(2);
  });

  it('should execute job', ()=>{
     spyOn(monitorService,'executeJob').and.returnValue(of("test"));
     component.executeJob("test");
     expect(monitorService.executeJob).toHaveBeenCalled();
  });

  it('should return 1 hour', ()=>{
      expect(component.getIntervalCheck(1, TimeFormat.H)).toContain('1 hour');
  });

  it('should return hours', ()=>{
      expect(component.getIntervalCheck(12, TimeFormat.H)).toContain('hours');
  });

});
