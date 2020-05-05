import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SSEService {

  constructor() { }

  getEventSource(url: string) {
    return new EventSource(url);
  }
}
