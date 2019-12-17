import {Component, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from "../modal.component";
import {NotificationService} from "../../../service/notification.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-modal-notification-send',
  templateUrl: './modal-notification-send.component.html',
  styleUrls: ['./modal-notification-send.component.css']
})
export class ModalNotificationSendComponent implements OnInit {

  @ViewChild(ModalComponent)
  public readonly modal: ModalComponent;

  private f: FormGroup = new FormGroup({
    'TITLE': new FormControl('', [Validators.required]),
    'text': new FormControl('', [Validators.required]),
  });

  private form: FormGroup = this.f;

  constructor(private notificationService: NotificationService) { }

  ngOnInit() {
    this.modal.setModalType('info');
    this.modal.setStatusOfIcons(true);
  }

  complete(): void {
    this.modal.hide();
    this.notificationService.sendMailAdmin({
      mailType: "BROADCAST",
      otherAttributes: this.form.value
    }).subscribe(
        done => {
          console.debug("Notification sent successfully");
        }, error => {
          console.debug(error)
        });
    this.form.reset(this.f);
  }

  public show(): void {
    this.modal.show();
  }

  public dismiss(): void {
    this.modal.hide();
    this.form.reset(this.f);
    console.debug('Notification modal dismissed');
  }

}
