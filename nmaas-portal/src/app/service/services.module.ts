import {NgModule} from '@angular/core';

import {AppConfigService} from './appconfig.service';
import {AppImagesService} from './appimages.service';
import {AppInstanceService} from './appinstance.service';
import {AppsService} from './apps.service';
import {ChangelogService} from './changelog.service';
import {AppSubscriptionsService} from './appsubscriptions.service';
import {DomainService} from './domain.service';
import {GenericDataService} from './genericdata.service';
import {TagService} from './tag.service';
import {UserService} from './user.service';


@NgModule({
  providers: [
    AppConfigService,
    AppImagesService,
    AppInstanceService,
    AppsService,
    AppSubscriptionsService,
    ChangelogService,
    DomainService,
    TagService,
    UserService,
  ]
})
export class ServicesModule {}
