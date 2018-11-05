import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {Observable, Subscription} from "rxjs";
import {ContentDisplayService} from "../../service/content-display.service";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

    public languages: string[];
    public refresh: Subscription;

    constructor(private translate: TranslateService, private contentService:ContentDisplayService) {
    }

    useLanguage(language: string) {
        this.translate.use(language);
    }

    getCurrent(){
        return this.translate.currentLang;
    }

    getPathToCurrent(){
        return "assets/images/country/" + this.getCurrent() + "_circle.png";
    }

    ngOnInit() {
        this.getSupportedLanguages()
    }

    public getSupportedLanguages(){
        this.contentService.getLanguages().subscribe(langs =>{
            this.translate.addLangs(langs);
            this.languages = langs;
        });
    }
}
