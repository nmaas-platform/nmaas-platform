import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalGuestUserComponent } from './modal-guest-user.component';
import {Component} from "@angular/core";
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {ModalComponent} from "../../../shared/modal";

@Component({
  selector: 'nmaas-modal',
  template:'<p>Modal Mock</p>'
})
class ModalMock {
  setModalType(arg: string) {

  }
  setStatusOfIcons(arg: boolean) {

  }
}

describe('ModalGuestUserComponent', () => {
  let component: ModalGuestUserComponent;
  let fixture: ComponentFixture<ModalGuestUserComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModalGuestUserComponent, ModalComponent ],
      imports: [
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader
          }
        }),
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModalGuestUserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
