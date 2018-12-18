import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {ClusterDetailsComponent} from "./clusterdetails.component";
import {ClusterDetailsComponent as ClusterSharedDetailsComponent} from "../../../../shared/admin/clusters/details/clusterdetails.component";
import {ClusterService} from "../../../../service/cluster.service";
import {RouterTestingModule} from "@angular/router/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AppConfigService} from "../../../../service";

describe('Cluster details component', () =>{
    let component: ClusterDetailsComponent;
    let fixture: ComponentFixture<ClusterDetailsComponent>;

    beforeEach(async (() =>{
        TestBed.configureTestingModule({
            declarations: [ClusterDetailsComponent, ClusterSharedDetailsComponent],
            imports: [
                FormsModule,
                ReactiveFormsModule,
                RouterTestingModule,
                HttpClientTestingModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                })
            ],
            providers: [ClusterService, AppConfigService]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ClusterDetailsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create component', () => {
        let app = fixture.debugElement.componentInstance;
        expect(app).toBeTruthy();
    });
});