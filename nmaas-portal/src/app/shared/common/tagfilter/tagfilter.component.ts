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
  
  @Output()
  public changed: EventEmitter<string> = new EventEmitter<string>();
  
  tags: Observable<string[]>;
  
  constructor(private tagService: TagService) { }

  ngOnInit() {
    this.tags = this.tagService.getTags();
  }

  public onChange(): void {    
    this.changed.emit(this.value);
  }
  
}
