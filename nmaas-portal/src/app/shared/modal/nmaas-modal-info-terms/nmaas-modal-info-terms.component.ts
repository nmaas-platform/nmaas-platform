import {Component, OnInit, Input, ViewChild} from '@angular/core';
import {ModalComponent} from '../../../shared/modal/index';
import {Content} from "../../../model/content";
import {ContentDisplayService} from "../../../service/content-display.service";
import {isUndefined} from "util";

@Component({
    selector: 'nmaas-modal-info-terms',
    templateUrl: './nmaas-modal-info-terms.component.html',
    styleUrls: ['./nmaas-modal-info-terms.component.css'],
    providers: [ModalComponent, ContentDisplayService]
})
export class NmaasModalInfoTermsComponent implements OnInit {

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;

    public content: Content;

    constructor(private contentDisplayService: ContentDisplayService) {
    }

    ngOnInit() {
        this.getContent();
    }

    getContent(): void{
        this.contentDisplayService.getContent("tos".toString()).subscribe(content=> this.content = content);
        if(!isUndefined(this.content) || this.content == null){
            this.modal.hide();
        }
    }


    public show(): void {
        this.modal.show();
    }

}
