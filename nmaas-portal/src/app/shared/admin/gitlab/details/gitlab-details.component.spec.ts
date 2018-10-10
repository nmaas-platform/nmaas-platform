import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GitlabDetailsComponent } from './gitlab-details.component';

describe('GitlabDetailsComponent', () => {
  let component: GitlabDetailsComponent;
  let fixture: ComponentFixture<GitlabDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GitlabDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GitlabDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

 // it('should create', () => {
  //  expect(component).toBeTruthy();
  //});
});
