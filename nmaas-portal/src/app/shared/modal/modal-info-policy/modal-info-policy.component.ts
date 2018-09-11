import {Component, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from "../index";
import {Content} from "../../../model/content";
import {ContentDisplayService} from "../../../service/content-display.service";
import {isNullOrUndefined} from "util";

@Component({
  selector: 'modal-info-policy',
  templateUrl: './modal-info-policy.component.html',
  styleUrls: ['./modal-info-policy.component.css'],
    providers: [ModalComponent, ContentDisplayService]
})
export class ModalInfoPolicyComponent implements OnInit {

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;

    public content: Content;

    constructor(private contentDisplayService: ContentDisplayService) {
    }

    ngOnInit() {
        this.modal.setModalType("success");
        this.getContent();
        if(typeof this.content.content != 'undefined' && this.content.content){
            this.content.content = "Default content";
        }
    }

    getContent(): void{
        this.contentDisplayService.getContent("pp").subscribe(content=> this.content = content);
        if(isNullOrUndefined(this.content)){
            this.modal.hide();
        }
    }

    public show(): void {
        this.modal.show();
    }

}
