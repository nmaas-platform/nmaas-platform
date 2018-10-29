import { Component, OnInit, Input, ViewEncapsulation, ViewChild } from '@angular/core';

import { ModalComponent } from '../modal/index';

import { AppsService, AppImagesService } from '../../service/index';
import { FileInfo } from '../../model/fileinfo';
import { GroupPipe, SecurePipe } from '../../pipe/index';
import {TranslateService} from "@ngx-translate/core";


@Component({
    selector: 'screenshots',
    templateUrl: './screenshots.component.html',
    styleUrls: ['./screenshots.component.css'],
    encapsulation: ViewEncapsulation.None,
    providers: [ModalComponent, AppsService, AppImagesService, GroupPipe, SecurePipe ]
})
export class ScreenshotsComponent implements OnInit {

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;

    @Input()
    public pathUrl: string;

    public imagesFileInfo: FileInfo[];

    public selectedImg: string;

    constructor(private appsService: AppsService, private translate:TranslateService) {
        const browserLang = translate.currentLang == null ? 'en' : translate.currentLang;
        translate.use(browserLang.match(/en|fr|pl/) ? browserLang : 'en');
    }

    ngOnInit() {
        this.appsService.getAppScreenshotsByUrl(this.pathUrl).subscribe(fileInfos => this.imagesFileInfo = fileInfos);
    }

    public showImage(url: string): void {
        this.selectedImg = url;
        this.modal.show();
    }

}
