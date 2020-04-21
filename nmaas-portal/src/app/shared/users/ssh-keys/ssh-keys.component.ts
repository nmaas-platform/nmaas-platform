import {Component, OnInit} from '@angular/core';
import {SSHKeyService} from '../../../service/sshkey.service';
import {Observable} from 'rxjs';
import {SSHKeyView} from '../../../model/sshkey-view';

@Component({
  selector: 'nmaas-ssh-keys',
  templateUrl: './ssh-keys.component.html',
  styleUrls: ['./ssh-keys.component.css']
})
export class SshKeysComponent implements OnInit {

  public keys: Observable<SSHKeyView[]> = undefined;

  constructor(private keyService: SSHKeyService) { }

  ngOnInit() {
    this.keys = this.keyService.getAll();
  }

}
