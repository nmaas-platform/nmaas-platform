import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {AuthModule} from '../../auth/auth.module';
import {SharedModule} from '../../shared/shared.module';
import {PipesModule} from '../../pipe/pipes.module';

import {AppInstanceComponent} from './appinstance/appinstance.component';
import {AppInstanceProgressModule} from './appinstanceprogress/appinstanceprogress.module';

import {AppsService} from '../../service/apps.service';
import {AppInstanceService} from '../../service/appinstance.service';
import {TagService} from '../../service/tag.service';
import {AppInstanceListComponent} from './appinstancelist/appinstancelist.component';
import {AppRestartModalComponent} from "../modals/apprestart";
import {
    JsonSchemaFormModule,
    Bootstrap3FrameworkModule,
    Bootstrap3Framework,
    Framework, WidgetLibraryService, FrameworkLibraryService, JsonSchemaFormService
} from "angular2-json-schema-form";
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {HttpClient} from '@angular/common/http';
import {HttpLoaderFactory} from '../../app.module';

@NgModule({
  declarations: [
    AppInstanceComponent,
    AppInstanceListComponent,
      AppRestartModalComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    SharedModule,
    AuthModule,
    AppInstanceProgressModule,
    PipesModule,
    Bootstrap3FrameworkModule,
    {
      ngModule: JsonSchemaFormModule,
      providers: [
        JsonSchemaFormService,
        FrameworkLibraryService,
        WidgetLibraryService,
        {provide: Framework, useClass: Bootstrap3Framework, multi: true}
      ]
    },
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    })
  ],
  exports: [
    AppInstanceComponent,
    AppInstanceListComponent
  ],
  providers: [
    AppsService,
    AppInstanceService,
    TagService
  ]

})
export class AppInstanceModule {}
