import {TestBed, async, ComponentFixture} from '@angular/core/testing';
import { LoginComponent } from './login.component';

import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterTestingModule} from "@angular/router/testing";
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {ModalComponent} from "../../shared/modal";
import {AuthService} from "../../auth/auth.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AppConfigService, ConfigurationService, DomainService, UserService} from "../../service";
import {ShibbolethService} from "../../service/shibboleth.service";
import {JwtModule} from "@auth0/angular-jwt";



describe('Component: Login', () => {
    let component:LoginComponent;
    let fixture:ComponentFixture<LoginComponent>;

    beforeEach(async(()=>{
        TestBed.configureTestingModule({
            declarations: [ LoginComponent, ModalComponent ],
            providers: [AuthService, AppConfigService, ShibbolethService, UserService, ConfigurationService, DomainService],
            imports: [
                HttpClientTestingModule,
                FormsModule,
                ReactiveFormsModule,
                JwtModule.forRoot(
                    {
                        config: {
                            tokenGetter: () => {
                                return '';
                            }
                        }}),
                RouterTestingModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                })]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create component', () => {
        let app = fixture.debugElement.componentInstance;
        expect(app).toBeTruthy();
    });

});
