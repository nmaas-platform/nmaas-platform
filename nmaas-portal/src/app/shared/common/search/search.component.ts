import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'nmaas-inline-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  @Input()
  public value: string;
  
  @Output()
  public submit: EventEmitter<string> = new EventEmitter<string>();
  
  @Output()
  public change: EventEmitter<string> = new EventEmitter<string>();
  
  constructor() { }

  ngOnInit() {
  }

  protected onSubmit(): void {
    this.submit.emit(this.value);
  }
  
  protected onChange(): void {
    this.change.emit(this.value);
  }
  
}
