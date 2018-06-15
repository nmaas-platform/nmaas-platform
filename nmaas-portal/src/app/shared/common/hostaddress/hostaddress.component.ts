import { HostAddress } from '../../../model/hostaddress';
import { BaseComponent } from '../../common/basecomponent/base.component';
import {ComponentMode} from '../../common/componentmode';
import {Component, Input, Output, EventEmitter, OnInit} from '@angular/core';

@Component({
    selector: 'nmaas-hostaddress',
    templateUrl: './hostaddress.component.html',
    styleUrls: ['./hostaddress.component.css']
})
export class HostAddressComponent extends BaseComponent implements OnInit {

    @Input()
    private hostAddress: HostAddress = new HostAddress();

    @Output()
    private onSave: EventEmitter<HostAddress> = new EventEmitter<HostAddress>();

    constructor() {
        super();
    }

    ngOnInit() {

    }

    public submit(): void {
        this.onSave.emit(this.hostAddress);
    }

    public onModeChange(): void {
        const newMode: ComponentMode = (this.mode === ComponentMode.VIEW ? ComponentMode.EDIT : ComponentMode.VIEW);
        if (this.isModeAllowed(newMode)) {
            this.mode = newMode;
        }
    }


}