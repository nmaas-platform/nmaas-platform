import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {MonitorDetailsComponent} from './monitor-details.component';
import {FormsModule} from '@angular/forms';
import {RouterTestingModule} from '@angular/router/testing';
import {MonitorService} from '../../../../service/monitor.service';
import {MonitorEntry} from '../../../../model/monitorentry';
import {Observable, of} from 'rxjs';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {PipesModule} from '../../../../pipe/pipes.module';

class MockMonitorService {
    private readonly url: string;

    constructor() {
        this.url = 'http://localhost/api';
    }

    public createMonitorEntryAndJob(monitorEntry: MonitorEntry): Observable<any> {
        return of<MonitorEntry>();
    }

    public updateMonitorEntryAndJob(monitorEntry: MonitorEntry): Observable<any> {
        return of<MonitorEntry>();
    }

    public deleteMonitorEntryAndJob(serviceName: string): Observable<any> {
        return of<MonitorEntry>();
    }

    public getAllMonitorEntries(): Observable<MonitorEntry[]> {
        return of<MonitorEntry[]>();
    }

    public getOneMonitorEntry(serviceName: string): Observable<MonitorEntry> {
        return of<MonitorEntry>();
    }

    public executeJob(serviceName: string): Observable<any> {
        return of<MonitorEntry>();
    }

    public resumeJob(serviceName: string): Observable<any> {
        return of<any>();
    }

    public pauseJob(serviceName: string): Observable<any> {
        return of<any>();
    }
}

describe('MonitorDetailsComponent', () => {
    let component: MonitorDetailsComponent;
    let fixture: ComponentFixture<MonitorDetailsComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [MonitorDetailsComponent],
            imports: [
                FormsModule,
                RouterTestingModule,
                PipesModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                }),
            ],
            providers: [
                {provide: MonitorService, useClass: MockMonitorService},
            ],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(MonitorDetailsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
