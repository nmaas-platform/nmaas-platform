import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AddMembersModalComponent} from './add-members-modal.component';
import {AppInstanceService, UserService} from '../../../../service';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {SharedModule} from '../../../../shared';
import {MultiSelectModule} from 'primeng/multiselect';
import {Role} from '../../../../model/userrole';
import {AppInstance, AppInstanceState, User} from '../../../../model';
import {ServiceAccessMethodType} from '../../../../model/service-access-method';
import createSpyObj = jasmine.createSpyObj;
import {of} from 'rxjs';
import {FormsModule} from '@angular/forms';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('AddMembersModalComponent', () => {
    let component: AddMembersModalComponent;
    let fixture: ComponentFixture<AddMembersModalComponent>;

    const appInstanceServiceStub: Partial<AppInstanceService> = {};

    const appInstance: AppInstance = {
        applicationId: 2,
        applicationName: 'Oxidized',
        configWizardTemplate: {
            id: 1,
            template: JSON.parse('{"title": "My Test Form","components": [{"type": "textfield", "input": true, "tableView": true, "inputType": "text", "inputMask": "", "label": "First Name", "key": "firstName", "placeholder": "Enter your first name", "prefix": "", "suffix": "", "multiple": false,"defaultValue": "","protected": false,"unique": false,"persistent": true,"validate": {"required": true,"minLength": 2,"maxLength": 10,"pattern": "","custom": "","customPrivate": false},"conditional": {"show": "","when": null,"eq": ""}},{"type": "textfield","input": true,"tableView": true,"inputType": "text","inputMask": "","label": "Last Name","key": "lastName","placeholder": "Enter your last name","prefix": "","suffix": "","multiple": false,"defaultValue": "","protected": false,"unique": false,"persistent": true,"validate": {"required": true, "minLength": 2, "maxLength": 10, "pattern": "", "custom": "", "customPrivate": false}, "conditional": {"show": "", "when": null, "eq": ""}}, {"input": true, "label": "Submit", "tableView": false, "key": "submit", "size": "md", "leftIcon": "", "rightIcon": "", "block": false, "action": "submit", "disableOnInvalid": true, "theme": "primary", "type": "button"}]}')
        },
        configUpdateWizardTemplate: {id: 2, template: '{"template":"xD"}'},
        configuration: '{"oxidizedUsername":"oxidized","oxidizedPassword":"oxi@PLLAB","targets":[{"ipAddress":"10.0.0.1"},{"ipAddress":"10.0.0.2"},{"ipAddress":"10.0.0.3"},{"ipAddress":"10.0.0.4"},{"ipAddress":"10.0.0.5"},{"ipAddress":"10.0.0.6"},{"ipAddress":"10.0.0.7"},{"ipAddress":"10.0.0.8"},{"ipAddress":"10.0.0.9"},{"ipAddress":"10.0.0.10"},{"ipAddress":"10.0.0.11"},{"ipAddress":"10.0.0.12"},{"ipAddress":"10.0.0.13"},{"ipAddress":"10.0.0.14"},{"ipAddress":"10.0.0.15"},{"ipAddress":"10.0.0.16"}]}',
        createdAt: new Date(),
        descriptiveDeploymentId: 'test-oxidized-48',
        domainId: 4,
        id: 1,
        internalId: 'eccbaf70-7fdd-401a-bb3e-b8659bcfbdff',
        name: 'oxi-virt-1',
        owner: {
            id: 1, username: 'admin', enabled: true,
            firstname: null, lastname: null,
            email: 'admin@admi.eu', selectedLanguage: 'en',
            privacyPolicyAccepted: true, ssoUser: false,
            termsOfUseAccepted: false, roles: [{domainId: 1, role: Role.ROLE_SYSTEM_ADMIN}]
        } as User,
        state: AppInstanceState.RUNNING,
        serviceAccessMethods: [
            {type: ServiceAccessMethodType.DEFAULT, name: 'Default link', protocol: 'Web', url: 'http://oxi-virt-1.test.nmaas.geant.org'},
            {type: ServiceAccessMethodType.EXTERNAL, name: 'Second link', protocol: 'Web', url: 'http://second.org'},
            {type: ServiceAccessMethodType.INTERNAL, name: 'Internal', protocol: 'SSH', url: 'internal'}
        ],
        userFriendlyState: 'Application instance is running',
        appConfigRepositoryAccessDetails: {
            cloneUrl: 'http://clone.me'
        },
        members: []
    };

    beforeEach(async(() => {

        const userServiceSpy = createSpyObj('UserService', ['getAll'])
        userServiceSpy.getAll.and.returnValue(of([]))

        TestBed.configureTestingModule({
            declarations: [AddMembersModalComponent],
            imports: [
                SharedModule,
                MultiSelectModule,
                FormsModule,
                BrowserAnimationsModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader
                    }
                })
            ],
            providers: [
                {provide: UserService, useValue: userServiceSpy},
                {provide: AppInstanceService, useValue: appInstanceServiceStub},
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AddMembersModalComponent);
        component = fixture.componentInstance;
        component.appInstance = appInstance;
        fixture.detectChanges();

    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
