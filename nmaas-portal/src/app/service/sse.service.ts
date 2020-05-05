import { Injectable } from '@angular/core';
import {EventSourcePolyfill} from 'ng-event-source';

@Injectable({
  providedIn: 'root'
})
export class SSEService {

  constructor() { }

  getEventSource(url: string) {
    return new EventSourcePolyfill(url, {headers: {'Authorization': 'Bearer ' + localStorage.getItem('token')}});
  }
}
