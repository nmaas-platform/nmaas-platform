import {Component, OnInit, ViewChild} from '@angular/core';
import {AppConfigService, ChangelogService} from "../../service";
import {GitInfo} from "../../model/gitinfo";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../service/notification.service";
import {Mail} from "../../model/mail";
import {TranslateService} from "@ngx-translate/core";
import {ModalComponent} from "../modal";
import {RecaptchaComponent} from "ng-recaptcha";

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

  @ViewChild(RecaptchaComponent)
  public captcha: RecaptchaComponent;

  public captchaToken:string = "";

  @ViewChild(ModalComponent)
  public readonly modal: ModalComponent;

  constructor(private changelogService:ChangelogService, private appConfigService:AppConfigService,
              private notificationService: NotificationService, private fb: FormBuilder, private translate: TranslateService) {
    this.mail = new Mail();
    this.mailForm = this.fb.group({
      email: ['',[Validators.required, Validators.email]],
      name: ['', [Validators.required]],
      message: ['', [Validators.required, Validators.maxLength(600)]]
    });
  }

  ngOnInit() {
    if(this.appConfigService.getShowGitInfo()){
      this.changelogService.getGitInfo().subscribe(info => this.gitInfo = info);
    }
  }

  public resolved(captchaResponse: string) {
    this.captchaToken = captchaResponse;
  }

  public sendMail(){
    if(this.captchaToken.length < 1){
      this.errorMessage = this.translate.instant('GENERIC_MESSAGE.NOT_ROBOT_ERROR_MESSAGE');
    } else{
      if(this.mailForm.valid){
        this.mail.otherAttributes = this.mailForm.getRawValue();
        this.mail.mailType = "CONTACT_FORM";
        this.notificationService.sendMail(this.mail).subscribe(()=> {
          this.errorMessage=undefined;
          this.mailForm.reset();
          this.captcha.reset();
          this.modal.show();
        }, error=>{
          this.errorMessage = error.message;
          this.captcha.reset();
        });
      }
    }
  }
}
