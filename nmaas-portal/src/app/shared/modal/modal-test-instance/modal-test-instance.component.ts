import {Component, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from '../modal.component';

@Component({
  selector: 'modal-test-instance',
  templateUrl: './modal-test-instance.component.html',
  styleUrls: ['./modal-test-instance.component.css'],
  providers: [ModalComponent]
})
export class ModalTestInstanceComponent implements OnInit {

  @ViewChild(ModalComponent)
  public readonly modal: ModalComponent;

  constructor() { }

  ngOnInit() {
    this.modal.setModalType('info');
    this.modal.setStatusOfIcons(true);
  }

}
