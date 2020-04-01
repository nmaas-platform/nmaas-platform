import {APP_INITIALIZER, NgModule} from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormsModule} from "@angular/forms";
import {RouterModule} from "@angular/router";
import {SharedModule} from "../../shared";
import {AuthModule} from "../../auth/auth.module";
import {PipesModule} from "../../pipe/pipes.module";
import {TranslateModule} from "@ngx-translate/core";
import {AppsService, TagService} from "../../service";
import { AppManagementListComponent } from './appmanagementlist/appmanagementlist.component';
import {StepsModule} from "primeng/steps";
import {ChipsModule, FileUploadModule, MultiSelectModule} from "primeng/primeng";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {AppCreateWizardComponent} from "./appcreatewizard/appcreatewizard.component";
import {InternationalizationService} from "../../service/internationalization.service";
import {FormioModule} from "angular-formio";
import {ConfigTemplateService} from "../../service/configtemplate.service";
import { AppChangeStateModalComponent } from './appchangestatemodal/appchangestatemodal.component';
import { AppPreviewComponent } from './apppreview/apppreview.component';
import {TooltipModule} from "ng2-tooltip-directive";
import { AppVersionCreateWizardComponent } from './appversioncreatewizard/appversioncreatewizard.component';
import { AppAccessMethodEditComponent } from './app-access-method-edit-component/app-access-method-edit.component';

export function getJsonTemplates(config: ConfigTemplateService) {
  return () => config.loadConfigTemplate();
}


@NgModule({
  declarations: [ AppManagementListComponent, AppCreateWizardComponent, AppChangeStateModalComponent, AppPreviewComponent, AppVersionCreateWizardComponent, AppAccessMethodEditComponent ],
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
    ChipsModule
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
        multi:true
      }
  ]
})
export class AppManagementModule { }
