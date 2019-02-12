import {APP_INITIALIZER, NgModule} from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterModule} from "@angular/router";
import {SharedModule} from "../../shared";
import {AuthModule} from "../../auth/auth.module";
import {PipesModule} from "../../pipe/pipes.module";
import {TranslateModule} from "@ngx-translate/core";
import {AppsService, TagService} from "../../service";
import { AppManagementListComponent } from './appmanagementlist/appmanagementlist.component';
import {StepsModule} from "primeng/steps";
import {FileUploadModule, MultiSelectModule} from "primeng/primeng";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {AppCreateWizardComponent} from "./appcreatewizard/appcreatewizard.component";
import {InternationalizationService} from "../../service/internationalization.service";
import {FormioModule} from "angular-formio";
import {ConfigTemplateService} from "../../service/configtemplate.service";

export function getJsonTemplates(config: ConfigTemplateService) {
  return () => config.loadConfigTemplate();
}


@NgModule({
  declarations: [ AppManagementListComponent, AppCreateWizardComponent ],
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
    ReactiveFormsModule,
    StepsModule,
    MultiSelectModule,
    FileUploadModule,
    BrowserAnimationsModule
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
