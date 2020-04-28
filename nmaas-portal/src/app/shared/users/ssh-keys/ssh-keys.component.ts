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
  public keysList: SSHKeyView[] = [];

  constructor(private keyService: SSHKeyService) { }

  ngOnInit() {
    this.keys = this.keyService.getAll();
    this.getData();
  }

  getData() {
      this.keysList = [];
      this.keys.subscribe(
          data => {
              this.keysList.push(...data);
          },
          error => {
              console.error(error);
          }
      )
  }

  invalidate(id: number) {
      this.keyService.invalidate(id).subscribe(
          data => {
              console.log('invalidating ssh key id: ' + id + ' success');
              this.getData();
          },
          error => {
              console.error(error);
          }
      );
  }

}
