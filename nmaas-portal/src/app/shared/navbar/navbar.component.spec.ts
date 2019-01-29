import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarComponent } from './navbar.component';
import {TranslateModule} from "@ngx-translate/core";
import {TranslateFakeLoader} from "@ngx-translate/core";
import {TranslateLoader} from "@ngx-translate/core";
import {ContentDisplayService} from "../../service/content-display.service";
import {Observable, of} from "rxjs";

class MockContentDisplayService{

    public getLanguages(): Observable<string[]>{
        return of([]);
    }
}

describe('NavbarComponent_Shared', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;
  let contentService: ContentDisplayService;
  let spy: any;

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
    contentService = fixture.debugElement.injector.get(ContentDisplayService);
    spy = spyOn(contentService, 'getLanguages').and.returnValue(of(['en', 'fr', 'pl']));
    component.useLanguage('en');
    fixture.detectChanges();
  });

  it('should create', () => {
      expect(component).toBeTruthy();
  });

  it('should change language',() =>{
      component.useLanguage("fr");
      expect(component.getCurrent()).toBe("fr");
  })
});
