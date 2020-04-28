import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NgTerminal} from 'ng-terminal';

@Component({
  selector: 'app-ssh-shell',
  templateUrl: './ssh-shell.component.html',
  styleUrls: ['./ssh-shell.component.css']
})
export class SshShellComponent implements OnInit, AfterViewInit, OnDestroy {

  private line = '';
  private socket: WebSocket;

  @ViewChild('term') child: NgTerminal; // for Angular 7
  // @ViewChild('term', { static: true }) child: NgTerminal; // for Angular 8

  constructor() { }

  ngOnInit() {
    // TODO move to service (temporary)
    this.socket = new WebSocket('ws://localhost:9000/ssh/servlet');
    this.socket.onopen = (event) => {
      console.log('Connection opened');
      console.log(event);
    };
    this.socket.onclose = (event) => {
      console.log('Connection closed');
      // console.log(event);
    };
    this.socket.onerror = (event) => {
      console.error(event);
    };
    this.socket.onmessage = (event) => {
      console.log(event.data);
      this.child.write(event.data + '\r\n$ ');
    };
  }

  ngAfterViewInit() {
    // terminal is available now
    // default handler with enhancement
    this.child.write('$ ');
    this.child.keyEventInput.subscribe(e => {
      // console.log('keyboard event:' + e.domEvent.keyCode + ', ' + e.key);

      const ev = e.domEvent;
      const printable = !ev.altKey && !ev.ctrlKey && !ev.metaKey;

      if (ev.keyCode === 13) { // enter
        this.socket.send(this.line);
        // console.debug('[LINE]: ' + this.line);
        this.line = '';
        this.child.write('\r\n');
      } else if (ev.keyCode === 8) { // backspace
        // Do not delete the prompt
        if (this.child.underlying.buffer.active.cursorX > 2) {
          this.child.write('\b \b');
        }
      } else if (printable) { // standard
        this.child.write(e.key);
        this.line += e.key;
      }
    })
  }

  ngOnDestroy(): void {
    this.socket.close();
  }

}
