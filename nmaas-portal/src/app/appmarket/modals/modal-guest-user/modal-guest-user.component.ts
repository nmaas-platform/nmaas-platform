import {Component, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from "../../../shared/modal";

@Component({
  selector: 'modal-guest-user',
  templateUrl: './modal-guest-user.component.html',
  styleUrls: ['./modal-guest-user.component.css']
})
export class ModalGuestUserComponent implements OnInit {

  @ViewChild(ModalComponent, { static: true })
  public readonly modal: ModalComponent;

  constructor() { }

  ngOnInit() {
    this.modal.setModalType('info');
    this.modal.setStatusOfIcons(true);
  }

}
