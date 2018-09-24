import {Component, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from "../../modal";
import {Content} from "../../../model/content";
import {ContentDisplayService} from "../../../service/content-display.service";
import {isNullOrUndefined} from "util";
import {Router} from "@angular/router";
import {ChangelogService} from "../../../service";

@Component({
  selector: 'app-modal-changelog',
  templateUrl: './modal-changelog.component.html',
  styleUrls: ['./modal-changelog.component.css']
})
export class ModalChangelogComponent implements OnInit {

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;

    changelog: any;

    constructor(private router: Router, private changelogService: ChangelogService) { }

    ngOnInit() {
        this.modal.setModalType("info");
        this.modal.setStatusOfIcons(true);
        this.getContent();
    }

    getContent(): void{
        this.changelogService.getChangelog().subscribe((changelog) => {
            this.changelog = changelog;
        })
    }

    public show(): void {
        this.modal.show();
    }

}
