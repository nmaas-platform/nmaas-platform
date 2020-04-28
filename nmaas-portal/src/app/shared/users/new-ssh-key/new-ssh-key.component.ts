import {Component, EventEmitter, OnInit, Output, ViewChild} from '@angular/core';
import {SSHKeyService} from '../../../service/sshkey.service';
import {SSHKeyRequest} from '../../../model/sshkey-request';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {ModalComponent} from '../../modal';

@Component({
  selector: 'app-new-ssh-key',
  templateUrl: './new-ssh-key.component.html',
  styleUrls: ['./new-ssh-key.component.css']
})
export class NewSshKeyComponent implements OnInit {

  @ViewChild(ModalComponent)
  public readonly modal: ModalComponent;

  @Output()
  public out: EventEmitter<any> = new EventEmitter<any>();

  public error: String = undefined;

  public requestForm: FormGroup = undefined;

  constructor(private keyService: SSHKeyService, private formBuilder: FormBuilder) { }

  ngOnInit() {
    this.modal.setModalType('info');
    this.modal.setStatusOfIcons(true);

    this.requestForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(16)]],
      key: ['', [Validators.required, Validators.pattern('^(ssh-rsa AAAAB3NzaC1yc2|ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNT|ecdsa-sha2-nistp384 AAAAE2VjZHNhLXNoYTItbmlzdHAzODQAAAAIbmlzdHAzOD|ecdsa-sha2-nistp521 AAAAE2VjZHNhLXNoYTItbmlzdHA1MjEAAAAIbmlzdHA1Mj|ssh-ed25519 AAAAC3NzaC1lZDI1NTE5|ssh-dss AAAAB3NzaC1kc3)[0-9A-Za-z+/]+[=]{0,3}( .*)?$')]],
    })
  }

  public create() {
    const request = new SSHKeyRequest();
    console.log(this.requestForm);
    request.name = this.requestForm.value.name;
    request.key = this.requestForm.value.key;
    this.keyService.createKey(request).subscribe(
        data => {
          this.error = undefined;
          this.requestForm.reset();
          this.modal.hide();
          this.out.emit();
        },
        error => {
          this.error = error.message;
        }
    );
  }

  get name() {
    return this.requestForm.get('name');
  }

  get key() {
    return this.requestForm.get('key');
  }

}
