import {NgModule} from '@angular/core';

import {AppConfigService} from './appconfig.service';
import {AppImagesService} from './appimages.service';
import {AppInstanceService} from './appinstance.service';
import {AppsService} from './apps.service';
import { AppSubscriptionsService } from './appsubscriptions.service';
import {DomainService} from './domain.service';
import {GenericDataService} from './genericdata.service';
import { JsonMapperService } from './jsonmapper.service';
import {TagService} from './tag.service';
import {UserService} from './user.service';


@NgModule({
  providers: [
    AppConfigService,
    AppImagesService,
    AppInstanceService,
    AppsService,
    AppSubscriptionsService,
    DomainService,
    TagService,
    UserService,
    JsonMapperService

  ]
})
export class ServicesModule {}
