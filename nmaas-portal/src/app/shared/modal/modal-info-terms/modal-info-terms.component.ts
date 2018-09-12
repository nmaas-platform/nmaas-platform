import {Component, OnInit, Input, ViewChild} from '@angular/core';
import {ModalComponent} from '../index';
import {Content} from "../../../model/content";
import {ContentDisplayService} from "../../../service/content-display.service";
import {isNullOrUndefined} from "util";

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
        this.modal.setModalType("warning");
        //this.getContent();
        this.fakeContent();
    }

    fakeContent(): void{
        this.content = new Content();
        this.content.title = "Ooops!"
        this.content.name = "Error occured";
        this.content.id = 0;
        this.content.content = "Warning! You're changing something important!";
    }

    getContent(): void{
        this.contentDisplayService.getContent("tos").subscribe(content=> this.content = content);
        if(isNullOrUndefined(this.content)){
            this.modal.hide();
        }
    }

    public show(): void {
        this.modal.show();
    }

}
