import {Component, OnDestroy, OnInit} from '@angular/core';
import {NavigationEnd, Router, RouterEvent} from "@angular/router";
import {filter} from "rxjs/operators";
import {Observable, Subscription} from "rxjs";

@Component({
  selector: 'app-page-not-found',
  templateUrl: './page-not-found.component.html',
  styleUrls: ['./page-not-found.component.css']
})
export class PageNotFoundComponent implements OnInit, OnDestroy{

  private previousUrl: string;
  private subscription: Subscription;

  constructor(private router: Router) { }

  ngOnInit() {
    // Current implementation should always redirect to main page (appmarket) because angular `looses context` after url is entered manually
    const events = this.router.events.pipe(filter(event => event instanceof NavigationEnd));
    this.subscription = events.subscribe(e => {
      console.log(e);
      if (e instanceof RouterEvent) {
        this.previousUrl = e.url;
        console.log('Previous:\t' + this.previousUrl)
      } else {
        console.log('Not a RouterEvent');
      }
    })
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  public redirect() {
    this.router.navigateByUrl(this.previousUrl);
  }

}
