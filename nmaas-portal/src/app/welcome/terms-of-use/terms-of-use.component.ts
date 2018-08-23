import {Component, OnInit, ViewChild} from '@angular/core';
import {ContentDisplayService} from "../../service/content-display.service";
import {Content} from "../../model/content";
import {ModalComponent} from "../../shared/modal";

@Component({
  selector: 'app-terms-of-use',
  templateUrl: './terms-of-use.component.html',
  styleUrls: ['./terms-of-use.component.css'],
    providers: [ModalComponent]
})
export class TermsOfUseComponent implements OnInit {

  constructor(protected contentDisplayService: ContentDisplayService) {}

  public content: Content;

  getContent(): void{
    this.contentDisplayService.getContent("tos".toString()).subscribe(content=> this.content = content);
  }

  ngOnInit() {
    this.getContent()
  }

}