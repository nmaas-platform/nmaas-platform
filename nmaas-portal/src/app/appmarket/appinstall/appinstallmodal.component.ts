import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'nmaas-appInstallModal',
    templateUrl: './appinstallmodal.component.html',
    styleUrls: ['./appinstallmodal.component.css']
})
export class AppInstallModalComponent implements OnInit {

    public visible = false;
    private visibleAnimate = false;
    constructor() { }

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
