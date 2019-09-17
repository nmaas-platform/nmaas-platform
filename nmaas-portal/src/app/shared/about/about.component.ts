import {Component, OnInit, ViewChild} from '@angular/core';
import {AppConfigService, ChangelogService} from "../../service";
import {GitInfo} from "../../model/gitinfo";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../service/notification.service";
import {Mail} from "../../model/mail";
import {ModalComponent} from "../modal";
import {ReCaptchaV3Service} from "ng-recaptcha";

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent implements OnInit {

  public gitInfo: GitInfo;

  public mail:Mail;

  public mailForm: FormGroup;

  public errorMessage: any;

  @ViewChild(ModalComponent)
  public readonly modal: ModalComponent;

  constructor(private changelogService:ChangelogService, private appConfigService:AppConfigService, private recaptchaV3Service: ReCaptchaV3Service,
              private notificationService: NotificationService, private fb: FormBuilder) {
    this.mail = new Mail();
    this.mailForm = this.fb.group({
      email: ['',[Validators.required, Validators.email]],
      name: ['', [Validators.required]],
      message: ['', [Validators.required, Validators.maxLength(600)]]
    });
  }

  ngOnInit() {
    this.modal.setModalType("info");
    if(this.appConfigService.getShowGitInfo()){
      this.changelogService.getGitInfo().subscribe(info => this.gitInfo = info);
    }
  }

  public sendMail() {
    if(this.mailForm.valid){
      this.recaptchaV3Service.execute('contactForm').subscribe((token)=> {
        this.mail.otherAttributes = this.mailForm.getRawValue();
        this.mail.mailType = "CONTACT_FORM";
        this.notificationService.sendMail(this.mail, token).subscribe(()=> {
          this.errorMessage=undefined;
          this.mailForm.reset();
          this.modal.show();
        }, error=>{
          this.errorMessage = error.message;
        });
      });
    }
  }
}
