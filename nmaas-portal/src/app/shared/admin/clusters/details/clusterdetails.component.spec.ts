import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ClusterDetailsComponent } from './clusterdetails.component';
import {FormsModule} from "@angular/forms";
import {RouterTestingModule} from "@angular/router/testing";
import {MissingTranslationHandler, TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {CustomMissingTranslationService} from "../../../../i18n/custommissingtranslation.service";

describe('ClusterDetailsComponent', () => {
    let component: ClusterDetailsComponent;
    let fixture: ComponentFixture<ClusterDetailsComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [ ClusterDetailsComponent ],
            imports: [
                FormsModule,
                RouterTestingModule,
                TranslateModule.forRoot({
                    missingTranslationHandler: {provide: MissingTranslationHandler, useClass: CustomMissingTranslationService},
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                })]
        })
            .compileComponents();
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

    it('should return correct length of maps', ()=>{
        expect(component.controllerConfigOption.size).toBe(3);
        expect(component.namespaceConfigOption.size).toBe(3);
        expect(component.resourceConfigOption.size).toBe(3);
    });

    it('should return array of keys', ()=>{
        expect(component.getKeys(component.resourceConfigOption)).toContain('Do nothing');
    });

    it('should track by id', ()=>{
        expect(component.trackByFn(1)).toBe(1);
    });

    it('should add new network', ()=>{
        component.addNetwork();
        expect(component.cluster.externalNetworks.length).toBe(1);
    });

    it('should add and remove network', ()=>{
        component.addNetwork();
        component.addNetwork();
        component.removeNetwork(1);
        expect(component.cluster.externalNetworks.length).toBe(1);
    });

});