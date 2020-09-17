import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AppInstanceService} from '../../../service';
import {TranslateService} from '@ngx-translate/core';

@Component({
    selector: 'app-appinstance-shell-view',
    templateUrl: './appinstance-shell-view.component.html',
    styleUrls: ['./appinstance-shell-view.component.css']
})
export class AppInstanceShellViewComponent implements OnInit {

    public appInstanceId: number = undefined;
    public podName: string = undefined;
    public appInstanceName: string = undefined;
    public ready = false;

    constructor(private route: ActivatedRoute,
                private router: Router,
                private appInstanceService: AppInstanceService,
                private translateService: TranslateService) {
    }

    ngOnInit() {
        this.route.params.subscribe(
            params => {
                this.appInstanceId = +params['id'];
                this.podName = params['podname'];
                this.appInstanceService.getAppInstance(this.appInstanceId).subscribe(
                    data => {
                        if (!data.application.application.appDeploymentSpec.allowSshAccess) {
                            this.notFound();
                        } else {
                            this.ready = true;
                            this.appInstanceName = data.name;
                        }
                    },
                    error => {
                        console.error(error);
                        this.notFound();
                    }
                )
            }
        );
    }

    private notFound(): void {
        const promise = this.router.navigateByUrl('/notfound');
        promise.then(result => {
            if (result) { console.log('Redirected'); } else { console.log('Failed'); }
        })
    }

}
