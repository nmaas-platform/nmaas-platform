import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalChangelogComponent } from './modal-changelog.component';

describe('ModalChangelogComponent', () => {
  let component: ModalChangelogComponent;
  let fixture: ComponentFixture<ModalChangelogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModalChangelogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModalChangelogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
