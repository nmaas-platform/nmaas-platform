import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GitlabDetailsComponent } from './gitlab-details.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {FormsModule} from '@angular/forms';
import {ComponentMode} from '../../..';

describe('GitlabDetailsComponent', () => {
  let component: GitlabDetailsComponent;
  let fixture: ComponentFixture<GitlabDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GitlabDetailsComponent ],
        imports: [
            FormsModule,
            RouterTestingModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useClass: TranslateFakeLoader
                }
            })]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GitlabDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create component', () => {
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should get mode VIEW', () => {
    component.onModeChange();
    expect(component.getCurrentMode()).toBe(ComponentMode.VIEW);
  });

});
