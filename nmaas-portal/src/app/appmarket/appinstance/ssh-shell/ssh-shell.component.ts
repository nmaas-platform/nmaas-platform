import {AfterViewInit, Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NgTerminal} from 'ng-terminal';
import {ShellClientService} from '../../../service/shell-client.service';
import {ModalComponent} from '../../../shared/modal';

@Component({
    selector: 'app-ssh-shell',
    templateUrl: './ssh-shell.component.html',
    styleUrls: ['./ssh-shell.component.css']
})
export class SshShellComponent implements OnInit, AfterViewInit, OnDestroy {

    private line = '';

    @ViewChild('disconnectModal', {static: true})
    public disconnectModal: ModalComponent;

    @Input()
    public appInstanceId: number = undefined;

    @Input()
    private podName: string = undefined;

    private sessionId: string = undefined;

    private minPosition = 0;

    @ViewChild('term', {static: true}) child: NgTerminal;

    constructor(private shellClientService: ShellClientService) {
    }

    /**
     * gets events stream for existing session
     * @param sessionId
     */
    private getEvents(sessionId: string): void {
        this.shellClientService.getServerSentEvent(sessionId).subscribe(
            event => {
                console.debug('Message:', event)
                let message = event.data;
                // this will work only when message containing redundant command is first after sending command
                // (no other dangling operations in background)
                if (this.line !== '' && message.startsWith(this.line)) { // remove incoming command if present
                    message = message.slice(this.line.length)
                }
                this.line = ''; // clear command entry
                if (message == null) {
                    console.error('empty message');
                } else if (message.endsWith('<#>NEWLINE<#>')) { // newline token
                    this.child.write(message.replace('<#>NEWLINE<#>', '') + '\r\n');
                } else {
                    this.child.write(message);
                    this.minPosition = message.length; // set new cursor position so prompt cannot be deleted
                }
            },
            sseError => {
                console.error('Error during sse connection attempt', sseError);
                this.sessionId = null;
            }
        );
    }

    /**
     * creates new session and gets events stream for this session
     */
    public connect(): void {
        if (this.appInstanceId != null) {
            this.shellClientService.initConnection(this.appInstanceId, this.podName).subscribe(
                sessionId => {
                    this.sessionId = sessionId;
                    console.log('Connecting, sessionId:', this.sessionId)
                    this.getEvents(sessionId);
                },
                connError => {
                    console.error(connError);
                    this.sessionId = null;
                }
            );
            this.line = '';
        } else {
            console.error('App instance id is undefined')
        }
    }

    public disconnect(): void {
        if (this.appInstanceId && this.sessionId) {
            console.log('disconnecting the shell')
            this.shellClientService.closeConnection(this.sessionId);
            this.sessionId = undefined;
        } else {
            console.log('could not disconnect the shell (probably already disconnected)')
        }
    }

    public disconnectWithModal(): void {
        this.disconnect();
        this.disconnectModal.show()
    }

    /**
     * TODO try to reconnect using the same session
     */
    public reconnect(): void {
        this.disconnect();
        this.child.underlying.reset();
        this.connect();
    }

    ngOnInit() {
        this.connect();
    }

    ngAfterViewInit() {
        // terminal is available now
        // default handler with enhancement
        this.child.keyEventInput.subscribe(e => {

            const ev = e.domEvent;
            const printable = !ev.altKey && !ev.ctrlKey && !ev.metaKey;

            if (e.key === '\r') { // enter - submit new command
                if (this.line === 'exit') {
                    this.disconnectWithModal();
                    this.child.underlying.reset();
                } else {
                    this.shellClientService.sendCommand(this.sessionId, {
                        'command': this.line
                    }).subscribe(
                        data => {
                            console.log('Command sent', this.line);
                        },
                        error => {
                            console.error(error);
                        }
                    );
                    // this.line = '';
                    // this.child.write('\r\n');
                }
            } else if (e.key === String.fromCharCode(127)) { // backspace (DEL) for some reason this is ascii 127 instead of 8
                // ev.keyCode === 8
                // Do not delete the prompt
                if (this.child.underlying.buffer.active.cursorX > this.minPosition) {
                    this.child.write('\b \b'); // write backspace
                    this.line = this.line.slice(0, -1); // remove last character from line
                }

            } else if (printable) { // standard
                // extend definition of printable characters
                const code = e.key.charCodeAt(0)
                console.debug('new char entered', code)
                // ascii printable characters
                if (32 <= code && code <= 126) {
                    this.child.write(e.key);
                    this.line += e.key;
                }
            }
        })
    }

    ngOnDestroy(): void {
        console.log('shell component on destroy')
        this.disconnect();
    }

}
