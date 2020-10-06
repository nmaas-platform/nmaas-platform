import {AfterContentChecked, AfterViewChecked, Component, OnInit} from '@angular/core';
import {ContentDisplayService} from '../../service/content-display.service';
import {Content} from '../../model/content';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-privacy-policy-subpage',
  templateUrl: './privacy-policy-subpage.component.html',
  styleUrls: ['./privacy-policy-subpage.component.css'],
})
export class PrivacyPolicySubpageComponent implements OnInit, AfterViewChecked, AfterContentChecked {

  private height = 0;

  public content: Content;

  constructor(private contentDisplayService: ContentDisplayService, private translate: TranslateService) {
  }

  ngOnInit() {
    this.onResize();
    this.getContent();
  }

  ngAfterContentChecked() {
    this.onResize();
  }

  ngAfterViewChecked() {
    this.onResize();
  }

  getContent(): void {
    this.contentDisplayService.getContent('pp').subscribe(content => this.content = content);
  }

  onResize() {
    // TODO rewrite this component not to use 'document' - use css instead
    this.height = document.getElementById('global-footer').offsetHeight;
    document.getElementById('welcome-container').style.marginBottom = `${this.height * 9 / 10 + 5}px`;
  }

}
