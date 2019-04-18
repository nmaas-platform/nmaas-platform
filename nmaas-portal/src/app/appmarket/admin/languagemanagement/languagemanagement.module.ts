import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LanguageListComponent } from './languagelist/languagelist.component';
import { LanguageDetailsComponent } from './languagedetails/languagedetails.component';
import {InternationalizationService} from "../../../service/internationalization.service";
import {RouterModule} from "@angular/router";
import {TranslateModule} from "@ngx-translate/core";
import {SharedModule} from "../../../shared";
import {FormsModule} from "@angular/forms";
import {InputSwitchModule} from "primeng/primeng";

@NgModule({
  declarations: [LanguageListComponent, LanguageDetailsComponent],
  imports: [
    CommonModule,
    FormsModule,
    InputSwitchModule,
    RouterModule,
    SharedModule,
    TranslateModule.forChild()
  ],
  providers: [InternationalizationService]
})
export class LanguageManagementModule { }
