import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HostAddressComponent } from './hostaddress.component';

describe('HostAddressComponent', () => {
    let component: HostAddressComponent;
    let fixture: ComponentFixture<HostAddressComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [ HostAddressComponent ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(HostAddressComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
