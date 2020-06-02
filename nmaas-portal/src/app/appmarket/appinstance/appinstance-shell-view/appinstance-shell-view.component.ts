import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

@Component({
    selector: 'app-appinstance-shell-view',
    templateUrl: './appinstance-shell-view.component.html',
    styleUrls: ['./appinstance-shell-view.component.css']
})
export class AppInstanceShellViewComponent implements OnInit {

    public appInstanceId: number = undefined;

    constructor(private route: ActivatedRoute) {
    }

    ngOnInit() {
        this.route.params.subscribe(
            params => {
                this.appInstanceId = +params['id'];
            }
        );
    }

}
