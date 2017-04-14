import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'nmaas-modal',
    templateUrl: './modal.component.html',
    styleUrls: ['./modal.component.css']
})
export class ModalComponent implements OnInit {

    public visible = false;
    private visibleAnimate = false;
    constructor() { 
    }

    ngOnInit() {
    }


    public show(): void {
        this.visible = true;
        setTimeout(() => this.visibleAnimate = true);
    }

    public hide(): void {
        this.visibleAnimate = false;
        setTimeout(() => this.visible = false, 300);
    }
}
