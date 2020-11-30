import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from '../../../../shared/modal';
import {PodInfo} from '../../../../model/podinfo';

@Component({
  selector: 'app-select-pod-modal',
  templateUrl: './select-pod-modal.component.html',
  styleUrls: ['./select-pod-modal.component.css']
})
export class SelectPodModalComponent implements OnInit {

  @ViewChild(ModalComponent, {static: true})
  public readonly modal: ModalComponent;

  @Input()
  public url = '';

  @Input()
  public pods: PodInfo[] = []

  constructor() { }

  ngOnInit(): void {
  }

  public show() {
    this.modal.show();
  }

  public hide() {
    this.modal.hide();
  }

}
