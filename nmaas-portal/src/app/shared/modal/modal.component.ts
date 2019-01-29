import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'nmaas-modal',
    templateUrl: './modal.component.html',
    styleUrls: ['./modal.component.css']
})
export class ModalComponent implements OnInit {

    @Input() styleModal: string;

    public visible = false;
    public isInfo = false;
    public isError = false;
    public isWarning = false;
    public isSuccess = false;
    public visibleAnimate = false;
    public isDefault = true;
    public isIconsEnabled = true;
    constructor() {}

    ngOnInit() {
        switch(this.styleModal){
            case 'info':
                this.isInfo = true;
                break;
            case 'error':
                this.isError = true;
                break;
            case 'warning':
                this.isWarning = true;
                break;
            case 'success':
                this.isSuccess = true;
                break;
            case 'default':
            default:
                this.isDefault = true;
                break;
        }
    }


    public show(): void {
        this.visible = true;
        setTimeout(() => this.visibleAnimate = true);
    }

    public hide(): void {
        this.visibleAnimate = false;
        setTimeout(() => this.visible = false, 300);
    }

    public resetModalStyle(): void{
        this.isError = false;
        this.isInfo = false;
        this.isSuccess = false;
        this.isWarning = false;
    }

    public setStatusOfIcons(isIconsEnabled: boolean): void{
        this.isIconsEnabled = isIconsEnabled;
    }

    public setModalType(typeOfModal: string): void{
        this.resetModalStyle();
        this.isDefault = false;
        switch(typeOfModal){
            case 'info':
                this.isInfo = true;
                break;
            case 'error':
                this.isError = true;
                break;
            case 'warning':
                this.isWarning = true;
                break;
            case 'success':
                this.isSuccess = true;
                break;
            case 'default':
                this.isDefault = true;
                break;
            default:
                console.log("Invalid choice of modal type");
                break;
        }
    }
}
