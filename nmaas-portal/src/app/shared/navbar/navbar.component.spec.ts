import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarComponent } from './navbar.component';
import {TranslateModule} from "@ngx-translate/core";
import {TranslateService} from "@ngx-translate/core";
import {TranslateFakeLoader} from "@ngx-translate/core";
import {TranslateLoader} from "@ngx-translate/core";
import {ContentDisplayService} from "../../service/content-display.service";
import {Observable} from "rxjs";

class MockContentDisplayService{

    public getLanguages(): Observable<string[]>{
        return Observable.of([]);
    }
}

describe('NavbarComponent_Shared', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NavbarComponent ],
        imports: [
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useClass: TranslateFakeLoader
                }
            })
        ],
        providers: [
            {provide: ContentDisplayService, useClass: MockContentDisplayService}
        ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
