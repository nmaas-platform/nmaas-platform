import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpHandler, HttpRequest, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class CORSHeaderInterceptor implements HttpInterceptor {
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const reqcopy = req.clone();
        reqcopy.headers.set('Content-Type', 'application/json');
        reqcopy.headers.set('Accept', 'application/json');
        return next.handle(reqcopy);

    }
}