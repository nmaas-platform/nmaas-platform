import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TagFilterComponent } from './tagfilter.component';

describe('TagfilterComponent', () => {
  let component: TagFilterComponent;
  let fixture: ComponentFixture<TagFilterComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TagFilterComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TagFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  //it('should create', () => {
  //  expect(component).toBeTruthy();
 // });
});
