import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AppImagesService, AppsService} from '../../../service';
import {AppDescription} from '../../../model/app-description';
import {TranslateService} from '@ngx-translate/core';
import {DomSanitizer} from '@angular/platform-browser';
import {ApplicationDTO} from '../../../model/application-dto';

@Component({
    selector: 'app-apppreview',
    templateUrl: './apppreview.component.html',
    styleUrls: ['./apppreview.component.css']
})
export class AppPreviewComponent implements OnInit {

    @Input()
    public app: ApplicationDTO;

    @Input()
    public logo: any;

    @Input()
    public screenshots: any[];

    public versionVisible = false;

    constructor(public route: ActivatedRoute, public appService: AppsService, public translate: TranslateService,
                public appImagesService: AppImagesService, public dom: DomSanitizer, private router: Router) {
    }

    ngOnInit() {
        if (!this.app) {
            this.route.params.subscribe(params => {
                if (params['id'] != null) {
                    this.appService.getApplicationDTO(params['id']).subscribe(
                        result => {
                            this.app = result;
                            this.getLogo(result.applicationBase.id);
                        },
                        err => {
                            console.error(err);
                            if (err.statusCode && (err.statusCode === 404 || err.statusCode === 401 || err.statusCode === 403)) {
                                this.router.navigateByUrl('/notfound');
                            }
                        });
                }
            });
        }
    }

    public getLogo(id: number): void {
        this.appImagesService.getLogoFile(id).subscribe(file => {
            this.logo = this.convertToProperImageFile(file);
        }, err => console.error(err.message));
    }

    private convertToProperImageFile(file: any) {
        const result: any = new File([file], 'uploaded file', {type: file.type});
        result.objectURL = this.dom.bypassSecurityTrustUrl(URL.createObjectURL(result));
        return result;
    }

    public getDescription(): AppDescription {
        if (this.app == null) {
            return;
        }
        return this.app.applicationBase.descriptions.find(val => val.language === this.translate.currentLang);
    }

    public getPathUrl(id: number): string {
        if ((id != null) && !isNaN(id)) {
            return '/apps/' + id + '/rate/my';
        } else {
            return '';
        }
    }

    public getValidLink(url: string): string {
        if (url == null) {
            return;
        }
        if (!url.startsWith('http://') && !url.startsWith('https://')) {
            return '//' + url;
        }
        return url;
    }

    public isVersionView(): boolean {
        return (this.app.application.version != null) && this.app.application.version !== '';
    }

    public showVersions() {
        this.versionVisible = !this.versionVisible;
    }

}
