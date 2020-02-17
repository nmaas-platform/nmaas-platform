import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PageNotFoundComponent} from './page-not-found.component';
import {RouterTestingModule} from "@angular/router/testing";
import {Component} from "@angular/core";

@Component({
    selector: 'app-navbar',
    template: '<p>Mock Navbar</p>'
})
class MockNavbar {
}

describe('PageNotFoundComponent', () => {
    let component: PageNotFoundComponent;
    let fixture: ComponentFixture<PageNotFoundComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [
                PageNotFoundComponent,
                MockNavbar
            ],
            imports: [
                RouterTestingModule,
            ]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(PageNotFoundComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
