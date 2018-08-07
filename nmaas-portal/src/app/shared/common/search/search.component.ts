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
  public submitted: EventEmitter<string> = new EventEmitter<string>();
  
  @Output()
  public changed: EventEmitter<string> = new EventEmitter<string>();
  
  constructor() { }

  ngOnInit() {
  }

  protected onSubmit(): void {
    this.submitted.emit(this.value);
  }
  
  protected onChange(): void {
    this.changed.emit(this.value);
  }
  
}
