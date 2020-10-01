import { Component, OnInit, Input, HostListener, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';

import { SortService } from '../../service/sort.service';
@Component({
  selector: '[sortable-column]',
  templateUrl: './sortable-column.component.html',
  styleUrls: ['./sortable-column.component.css']
})
export class SortableColumnComponent implements OnInit, OnDestroy {

  @Input('sortable-column')
  columnName: string;

  @Input('sort-direction')
  sortDirection = '';

  private columnSortedSubscription: Subscription;

  constructor(private sortService: SortService) { }

  @HostListener('click')
  sort() {
    this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    this.sortService.columnSorted({ sortColumn: this.columnName, sortDirection: this.sortDirection });
  }

  ngOnInit() {
    this.columnSortedSubscription = this.sortService.columnSorted$.subscribe(event => {
      if (this.columnName != event.sortColumn) {
        this.sortDirection = '';
      }
    });
  }

  ngOnDestroy() {
    this.columnSortedSubscription.unsubscribe();
  }

}
