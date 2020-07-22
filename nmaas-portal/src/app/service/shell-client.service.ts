import {Injectable, NgZone} from '@angular/core';
import {SSEService} from './sse.service';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {AppConfigService} from './appconfig.service';
import {EventSourcePolyfill, OnMessageEvent} from 'ng-event-source';
import {PodInfo} from '../model/podinfo';

@Injectable({
  providedIn: 'root'
})
export class ShellClientService {

  private events: EventSourcePolyfill = undefined;

  constructor(private _zone: NgZone, private _sseService: SSEService, private http: HttpClient, private appConfig: AppConfigService) { }

  public initConnection(id: number, pod: string): Observable<string> {
    // @ts-ignore
    return this.http.post<string>(this.appConfig.getApiUrl() + '/shell/' + id + '/init/' + pod, {}, {responseType: 'text'});
  }

  public sendCommand(sessionId: string, command: Object = {}): Observable<any> {
    return this.http.post(this.appConfig.getApiUrl() + '/shell/' + sessionId + '/command', command);
  }

  /**
   * closes event stream, sends request to complete the session
   */
  public closeConnection(sessionId: string) {
    this.closeEventStream()
    this.http.delete(this.appConfig.getApiUrl() + '/shell/' + sessionId)
  }

  public closeEventStream() {
    this.events.close();
    this.events = undefined;
  }

  public getServerSentEvent(sessionId: string): Observable<OnMessageEvent> {
    return new Observable<OnMessageEvent>(observableEvents => {
      const events = this._sseService.getEventSource(this.appConfig.getApiUrl() + '/shell/' + sessionId);

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

      this.events = events;
    });
  }

  public getPossiblePods(id: number): Observable<PodInfo[]> {
    return this.http.get<PodInfo[]>(this.appConfig.getApiUrl() + '/shell/' + id + '/podnames');
  }

}
