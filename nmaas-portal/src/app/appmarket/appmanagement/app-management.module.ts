import {APP_INITIALIZER, NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {SharedModule} from '../../shared';
import {AuthModule} from '../../auth/auth.module';
import {PipesModule} from '../../pipe/pipes.module';
import {TranslateModule} from '@ngx-translate/core';
import {AppsService, TagService} from '../../service';
import {AppManagementListComponent} from './app-management-list/appmanagementlist.component';
import {StepsModule} from 'primeng/steps';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {AppCreateWizardComponent} from './app-create-wizard/appcreatewizard.component';
import {InternationalizationService} from '../../service/internationalization.service';
import {FormioModule} from 'angular-formio';
import {ConfigTemplateService} from '../../service/configtemplate.service';
import {AppChangeStateModalComponent} from './app-change-state-modal/appchangestatemodal.component';
import {AppPreviewComponent} from './app-preview/apppreview.component';
import {TooltipModule} from 'ng2-tooltip-directive';
import {AppVersionCreateWizardComponent} from './app-version-create-wizard/appversioncreatewizard.component';
import {AppAccessMethodEditComponent} from './app-access-method-edit-component/app-access-method-edit.component';
import {AppStorageVolumeEditComponent} from './app-storage-volume-edit-component/app-storage-volume-edit.component';
import {AppStaticGlobalDeployParametersEditComponent} from './app-static-global-deploy-parameters-edit/app-static-global-deploy-parameters-edit.component';
import {MultiSelectModule} from 'primeng/multiselect';
import {FileUploadModule} from 'primeng/fileupload';
import {ChipsModule} from 'primeng/chips';


export function getJsonTemplates(config: ConfigTemplateService) {
    return () => config.loadConfigTemplate();
}


@NgModule({
    declarations: [
        AppManagementListComponent,
        AppCreateWizardComponent,
        AppChangeStateModalComponent,
        AppPreviewComponent,
        AppVersionCreateWizardComponent,
        AppAccessMethodEditComponent,
        AppStorageVolumeEditComponent,
        AppStaticGlobalDeployParametersEditComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        SharedModule,
        AuthModule,
        PipesModule,
        TranslateModule.forChild(),
        FormioModule,
        RouterModule,
        StepsModule,
        MultiSelectModule,
        FileUploadModule,
        BrowserAnimationsModule,
        TooltipModule,
        ChipsModule,
        ReactiveFormsModule
    ],
    exports: [],
    providers: [
        AppsService,
        TagService,
        InternationalizationService,
        ConfigTemplateService,
        {
            provide: APP_INITIALIZER,
            useFactory: getJsonTemplates,
            deps: [ConfigTemplateService],
            multi: true
        }
    ]
})
export class AppManagementModule {
}
