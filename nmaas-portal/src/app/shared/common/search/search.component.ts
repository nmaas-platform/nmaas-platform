import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'nmaas-inline-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  @Input()
  public value: string;
  
  @Output()
  public submitted: EventEmitter<string> = new EventEmitter<string>();
  
  @Output()
  public changed: EventEmitter<string> = new EventEmitter<string>();
  
  constructor(private translate:TranslateService) {
      const browserLang = translate.currentLang == null ? 'en' : translate.currentLang;
      translate.use(browserLang.match(/en|fr|pl/) ? browserLang : 'en');
  }

  ngOnInit() {
  }

  public onSubmit(): void {
    this.submitted.emit(this.value);
  }
  
  public onChange(): void {
    this.changed.emit(this.value);
  }
  
}
