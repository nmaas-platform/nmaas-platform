import {Component, OnInit, ViewEncapsulation, Input} from '@angular/core';
import {Application} from '../../../../model/application';
import {AppImagesService} from '../../../../service/appimages.service';
import {RateComponent} from '../../../rate/rate.component';
import {DefaultLogo} from '../../../../directive/defaultlogo.directive';

import {isUndefined} from 'util';
import {SecurePipe} from '../../../../pipe/index';

@Component({
  selector: 'nmaas-applist-element',
  providers: [DefaultLogo, RateComponent, AppImagesService, SecurePipe],
  templateUrl: './appelement.component.html',
  styleUrls: ['./appelement.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class AppElementComponent implements OnInit {

  @Input()
  public app: Application;

  @Input()
  public selected: boolean;

  constructor(public appImagesService: AppImagesService) {
  }

  ngOnInit() {
    if (isUndefined(this.selected)) {
      this.selected = false;
    }
  }
}
