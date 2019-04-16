import { Component, OnInit } from '@angular/core';
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-service-unavailable',
  templateUrl: './service-unavailable.component.html',
  styleUrls: ['./service-unavailable.component.css']
})
export class ServiceUnavailableComponent implements OnInit {

  constructor(private translateService: TranslateService) { }

  ngOnInit() {

  }

  public changeLang(lang: string): void{
    console.debug("lang_change: ", lang);
    this.translateService.use(lang);
  }

}
