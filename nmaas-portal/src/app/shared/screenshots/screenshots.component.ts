import { Component, OnInit, Input, ViewEncapsulation } from '@angular/core';

import { AppsService } from '../../service/apps.service';
import { FileInfo } from '../../model/fileinfo';

@Component({
    selector: 'screenshots',
    templateUrl: './screenshots.component.html',
    styleUrls: ['./screenshots.component.css'],
    encapsulation: ViewEncapsulation.None,
    providers: [AppsService]
})
export class ScreenshotsComponent implements OnInit {

    @Input()
    private pathUrl: string;

    imagesFileInfo: FileInfo[];

    constructor(private appsService: AppsService) { }

    ngOnInit() {
        this.appsService.getAppScreenshotsByUrl(this.pathUrl).subscribe(fileInfos => this.imagesFileInfo = fileInfos);
    }

}