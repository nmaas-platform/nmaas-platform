import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from "../../../../shared/modal";
import {AppInstanceService} from "../../../../service";

@Component({
  selector: 'nmaas-modal-app-abort',
  templateUrl: './app-abort-modal.component.html',
  styleUrls: ['./app-abort-modal.component.css'],
    providers:[ModalComponent]
})
export class AppAbortModalComponent implements OnInit {

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;

    @Input()
    private appInstanceId: number;

    @Input()
    private domainId: number;

    constructor(private appInstanceService:AppInstanceService) {
    }

    ngOnInit() {

    }

    public show() {
        this.modal.show();
    }

    public abort() {
        this.appInstanceService.removeAppInstance(this.appInstanceId).subscribe(() => this.modal.hide());
    }
}
