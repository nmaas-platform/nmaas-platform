import {Component, OnInit, Input, ViewChild} from '@angular/core';
import {ModalComponent} from '../index';
import {Content} from "../../../model/content";
import {ContentDisplayService} from "../../../service/content-display.service";
import {isNullOrUndefined, isUndefined} from "util";

@Component({
    selector: 'modal-info-terms',
    templateUrl: './modal-info-terms.component.html',
    styleUrls: ['./modal-info-terms.component.css'],
    providers: [ModalComponent, ContentDisplayService]
})
export class ModalInfoTermsComponent implements OnInit {

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;

    public content: Content;

    constructor(private contentDisplayService: ContentDisplayService) {
    }

    ngOnInit() {
        this.getContent();
    }

    getContent(): void{
        this.contentDisplayService.getContent("sos").subscribe(content=> this.content = content);
        if(isNullOrUndefined(this.content)){
            this.modal.hide();
        }
    }


    public show(): void {
        this.modal.show();
    }

}
