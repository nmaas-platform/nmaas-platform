import { AppConfigService } from '../service/appconfig.service';
import {Component, ComponentFactoryResolver, NgModule, OnInit} from '@angular/core';

@Component({
  selector: 'app-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent implements OnInit {

  constructor(private appConfig: AppConfigService) { }

  ngOnInit() {

  }

  
}
