import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { Router, ActivatedRoute, Params }   from '@angular/router';

import { Application } from '../../model/application';
import { ModalComponent } from '../../shared/modal/index';
import { AppInstanceService } from '../../service/appinstance.service';

@Component({
    selector: 'nmaas-appInstallModal',
    templateUrl: './appinstallmodal.component.html',
    styleUrls: ['./appinstallmodal.component.css'],
     providers: [ AppInstanceService, ModalComponent ]
})
export class AppInstallModalComponent implements OnInit {

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;

    @Input()
    app: Application;
    
    name: string;
    
    constructor(private appInstanceService: AppInstanceService, private router: Router) { 
    }
    
    ngOnInit() {
    }
    
    public create(): void {
        this.appInstanceService.createAppInstance(this.app.id, this.name).subscribe(
            instanceId => {
                this.modal.hide();
                this.router.navigate(['/instances', instanceId.id]);
            });
    }
    
    public show(): void {
        this.modal.show();
    }
    
}
