import {Injectable, NgZone} from '@angular/core';
import {SSEService} from './sse.service';
import {observable, Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ShellClientService {

  constructor(private _zone: NgZone, private _sseService: SSEService) { }

  getServerSentEvent(url: string): Observable<MessageEvent> {
    return new Observable<MessageEvent>(observableEvents => {
      const events = this._sseService.getEventSource(url);

      events.onopen = onopenEvent => {
        this._zone.run(() => {
          observableEvents.next(onopenEvent);
        })
      };

      events.onmessage = onmessageEvent => {
        this._zone.run(() => {
          observableEvents.next(onmessageEvent);
        })
      };

      events.onerror = onerrorEvent => {
        this._zone.run(() => {
          observableEvents.error(onerrorEvent);
        })
      };
    });
  }
}
