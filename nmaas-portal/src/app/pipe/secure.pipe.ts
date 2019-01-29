
import {throwError as observableThrowError, Subscription, Subscriber, Observable, BehaviorSubject} from 'rxjs';
import {Pipe, PipeTransform, OnDestroy, WrappedValue, ChangeDetectorRef, Injectable} from '@angular/core';

import {HttpClient, HttpParams} from '@angular/common/http';







import {DomSanitizer, SafeUrl} from '@angular/platform-browser';
import {catchError} from 'rxjs/operators';


@Injectable()
export class AuthHttpWrapper {
  constructor(private http: HttpClient) {

  }


  public get(url: string): Observable<any> {
    console.debug('Get secured url ' + url);
    if (!url) {
      return observableThrowError('Empty url');
    }

    return new Observable<Blob>((observer: Subscriber<any>) => {
      let objectUrl: string = null;

      this.http
        .get(url, {responseType: 'blob'}).pipe(
            catchError((error: Response | any) => {
              var errMsg: string = 'Unable to get ' + url;
              console.debug(errMsg);
              return observableThrowError(errMsg);
            }))
          .subscribe(m => {
            objectUrl = URL.createObjectURL(m);
            observer.next(objectUrl);
          });

      return () => {
        if (objectUrl) {
          URL.revokeObjectURL(objectUrl);
          objectUrl = null;
        }
      };
    });
  }
}

@Pipe({
  name: 'secure',
  pure: false
})
export class SecurePipe implements PipeTransform, OnDestroy {

  private latestValue: any = null;
  private latestReturnedValue: any = null;
  private subscription: Subscription = null;
  private obj: Observable<any> = null;

  private previousUrl: string;
  private resultSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);
  private result: Observable<any> = this.resultSubject.asObservable();
  private internalSubscription: Subscription = null;

  constructor(private ref: ChangeDetectorRef, private authHttp: AuthHttpWrapper, private sanitizer: DomSanitizer) {
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.dispose();
    }
  }

  transform(url: string): any {
    const obj = this.internalTransform(url);
    return this.asyncTransform(obj);
  }

  private internalTransform(url: string): Observable<any> {
    if (!url) {
      return this.result;
    }

    if (this.previousUrl !== url) {
      this.previousUrl = url;
      this.internalSubscription = this.authHttp.get(url).subscribe(m => {
        const sanitized = this.sanitizer.bypassSecurityTrustUrl(m);
        this.resultSubject.next(sanitized);
      });
    }

    return this.result;
  }

  private asyncTransform(obj: Observable<any>): any {
    if (!this.obj) {
      if (obj) {
        this.subscribe(obj);
      }
      this.latestReturnedValue = this.latestValue;
      return this.latestValue;
    }
    if (obj !== this.obj) {
      this.dispose();
      return this.asyncTransform(obj);
    }
    if (this.latestValue === this.latestReturnedValue) {
      return this.latestReturnedValue;
    }
    this.latestReturnedValue = this.latestValue;
    return WrappedValue.wrap(this.latestValue);
  }

  private subscribe(obj: Observable<any>) {
    const _this = this;
    this.obj = obj;

    this.subscription = obj.subscribe({
      next: function(value) {
        return _this.updateLatestValue(obj, value);
      }, error: (e: any) => {throw e; }
    });
  }

  private dispose() {

    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    this.subscription = null;

    if (this.internalSubscription) {
      this.internalSubscription.unsubscribe();
    }
    this.internalSubscription = null;

    this.latestValue = null;
    this.latestReturnedValue = null;

    this.obj = null;
  }

  private updateLatestValue(async: any, value: Object) {
    if (async === this.obj) {
      this.latestValue = value;
      this.ref.markForCheck();
    }
  }

}
