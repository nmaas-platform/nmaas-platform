import { TagService } from '../../../service/tag.service';
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Observable } from 'rxjs/Observable';

@Component({
  selector: 'nmaas-tag-filter',
  templateUrl: './tagfilter.component.html',
  styleUrls: ['./tagfilter.component.css']
})
export class TagFilterComponent implements OnInit {

  @Input()
  public value: string;

  public all: string = 'all';
  
  @Output()
  public change: EventEmitter<string> = new EventEmitter<string>();
  
  public tags: Observable<string[]>;
  
  constructor(private tagService: TagService) { }

  ngOnInit() {
    this.tags = this.tagService.getTags();
  }

  public onChange($event): void {    
    this.change.emit(this.value !== 'all' ? this.value : null);
  }
  
}
