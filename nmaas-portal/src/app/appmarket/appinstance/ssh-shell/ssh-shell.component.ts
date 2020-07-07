import {AfterViewInit, Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NgTerminal} from 'ng-terminal';
import {ShellClientService} from '../../../service/shell-client.service';
import {isNullOrUndefined} from 'util';

@Component({
  selector: 'app-ssh-shell',
  templateUrl: './ssh-shell.component.html',
  styleUrls: ['./ssh-shell.component.css']
})
export class SshShellComponent implements OnInit, AfterViewInit, OnDestroy {

  private line = '';

  @Input()
  private appInstanceId: number = undefined;

  @Input()
  private podName: string = undefined;

  private sessionId: string = undefined;

  private minPosition = 0;

  @ViewChild('term') child: NgTerminal; // for Angular 7
  // @ViewChild('term', { static: true }) child: NgTerminal; // for Angular 8

  constructor(private shellClientService: ShellClientService) { }

  ngOnInit() {
      if (!isNullOrUndefined(this.appInstanceId)) {
          this.shellClientService.initConnection(this.appInstanceId, this.podName).subscribe(
              sessionId => {
                  this.sessionId = sessionId;
                  this.shellClientService.getServerSentEvent(sessionId).subscribe(
                      event => {
                          console.log('Message:', event)
                          const mesg = event.data;
                          if (mesg == null) {
                              console.error('empty message');
                          } else if (mesg.endsWith('<#>NEWLINE<#>')) { // newline token
                              this.child.write(mesg.replace('<#>NEWLINE<#>', '') + '\r\n');
                          } else {
                              this.child.write(mesg);
                              this.minPosition = mesg.length; // set new cursor position so prompt cannot be deleted
                          }
                          // this.child.write(event.data + '\r\n$ ');
                      },
                      sseError => {
                          console.error(sseError);
                          this.sessionId = null;
                      }
                  );
              },
              connError => {
                  console.error(connError);
                  this.sessionId = null;
              }
          );
      } else {
          console.error('App instance id is undefined')
      }
  }

  ngAfterViewInit() {
    // terminal is available now
    // default handler with enhancement
    this.child.keyEventInput.subscribe(e => {
      // console.log('keyboard event:' + e.domEvent.keyCode + ', ' + e.key);

      const ev = e.domEvent;
      const printable = !ev.altKey && !ev.ctrlKey && !ev.metaKey;

      if (e.key === '\r') { // enter - submit new command
        this.shellClientService.sendCommand(this.sessionId, {
          'command': this.line
        }).subscribe(
            data => {
              console.log('Command sent');
            },
            error => {
              console.error(error);
            }
        );
        this.line = '';
        this.child.write('\r\n');

      } else if (e.key === String.fromCharCode(127)) { // backspace (DEL) for some reason this is ascii 127 instead of 8
          // ev.keyCode === 8
        // Do not delete the prompt
        if (this.child.underlying.buffer.active.cursorX > this.minPosition) {
          this.child.write('\b \b'); // write backspace
          this.line = this.line.slice(0, -1); // remove last character from line
        }

      } else if (printable) { // standard
        this.child.write(e.key);
        this.line += e.key;
      }
    })
  }

  ngOnDestroy(): void {
      if (this.appInstanceId) {
          this.shellClientService.close();
      }
  }

}
