import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from "../../../shared/modal";
import {AppInstanceService} from "../../../service";

@Component({
  selector: 'nmaas-modal-app-restart',
  templateUrl: './app-restart-modal.component.html',
  styleUrls: ['./app-restart-modal.component.css'],
    providers:[ModalComponent]
})
export class AppRestartModalComponent implements OnInit {

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;

    @Input()
    private appInstanceId: number;

    constructor(private appInstanceService:AppInstanceService) { }

    ngOnInit() {

    }

    public show(){
        this.modal.show();
    }

    public restart(){
        this.appInstanceService.restartAppInstance(this.appInstanceId).subscribe(suc=>this.modal.hide());
    }
}
