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
        this.modal.setModalType("info");
        //this.getContent();
        this.fakeContent();
    }

    fakeContent(): void{
        this.content = new Content();
        this.content.title = "Registration done"
        this.content.name = "Error occured";
        this.content.id = 0;
        this.content.content = "Default information";
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
