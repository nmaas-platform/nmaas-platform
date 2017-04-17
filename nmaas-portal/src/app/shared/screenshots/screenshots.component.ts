import { Component, OnInit, Input, ViewEncapsulation, ViewChild } from '@angular/core';

import { ModalComponent } from '../index';

import { AppsService, AppImagesService } from '../../service/index';
import { FileInfo } from '../../model/fileinfo';
import { GroupPipe } from '../../pipe/group.pipe';


@Component({
    selector: 'screenshots',
    templateUrl: './screenshots.component.html',
    styleUrls: ['./screenshots.component.css'],
    encapsulation: ViewEncapsulation.None,
    providers: [ModalComponent, AppsService, AppImagesService, GroupPipe ]
})
export class ScreenshotsComponent implements OnInit {

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;
    
    @Input()
    private pathUrl: string;

    imagesFileInfo: FileInfo[];
    
    private selectedImg: string;

    constructor(private appsService: AppsService) { }

    ngOnInit() {
        this.appsService.getAppScreenshotsByUrl(this.pathUrl).subscribe(fileInfos => this.imagesFileInfo = fileInfos);
    }

    public showImage(url: string): void {
        this.selectedImg = url;
        this.modal.show();
    }
    
}