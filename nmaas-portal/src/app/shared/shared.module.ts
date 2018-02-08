import { DefaultLogo } from '../directive/defaultlogo.directive';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

import {CommentsComponent} from './comments/index';
import {FooterComponent} from './footer/index';
import {RateComponent} from './rate/index';
import {ScreenshotsComponent} from './screenshots/index';
import {ModalComponent} from './modal/index';
import {PipesModule} from '../pipe/pipes.module';
import {ServicesModule} from '../service/services.module';
import {ComponentMode} from './common/componentmode';
import {PasswordComponent} from './common/password/password.component';
import {UserDetailsComponent} from './users/details/userdetails.component';
import {UsersListComponent} from './users/list/userslist.component';
import { UserPrivilegesComponent } from './users/privileges/userprivileges.component';
import { BaseComponent } from './common/basecomponent/base.component';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    CommonModule,
    PipesModule,
    FormsModule,
    ServicesModule,
    RouterModule,
    ReactiveFormsModule
  ],
  declarations: [
    RateComponent,
    FooterComponent,
    CommentsComponent,
    ScreenshotsComponent,
    ModalComponent,
    UserDetailsComponent,
    UsersListComponent,
    PasswordComponent,
    UserPrivilegesComponent,
    BaseComponent,
    DefaultLogo
  ],
  providers: [
  ],
  exports: [
    RateComponent,
    FooterComponent,
    CommentsComponent,
    ScreenshotsComponent,
    ModalComponent,
    UserDetailsComponent,
    UsersListComponent,
    PasswordComponent,
    UserPrivilegesComponent
  ]
})
export class SharedModule {}
