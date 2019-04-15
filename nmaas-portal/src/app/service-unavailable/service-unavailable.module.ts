import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ServiceUnavailableComponent } from './service-unavailable.component';
import {RouterModule} from "@angular/router";
import {ServiceUnavailableRoutes} from "./service-unavailable.routes";

@NgModule({
  declarations: [
    ServiceUnavailableComponent
  ],
  imports: [
    CommonModule,
    RouterModule
  ],
  exports: [
  ]
})
export class ServiceUnavailableModule { }
