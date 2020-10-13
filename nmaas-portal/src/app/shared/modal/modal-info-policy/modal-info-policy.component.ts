import {Component, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from '../index';
import {Content} from '../../../model/content';
import {ContentDisplayService} from '../../../service/content-display.service';

@Component({
  selector: 'modal-info-policy',
  templateUrl: './modal-info-policy.component.html',
  styleUrls: ['./modal-info-policy.component.css'],
})
export class ModalInfoPolicyComponent implements OnInit {

    @ViewChild(ModalComponent, { static: true })
    public readonly modal: ModalComponent;

    public content: Content;

    constructor(private contentDisplayService: ContentDisplayService) {
    }

    ngOnInit() {
        this.modal.setModalType('info');
        this.modal.setStatusOfIcons(true);
        this.getContent();
    }

    getContent(): void {
        this.contentDisplayService.getContent('pp').subscribe(content => this.content = content);
        if (this.content == null) {
            this.modal.hide();
        }
    }

    public show(): void {
        this.modal.show();
    }

}
