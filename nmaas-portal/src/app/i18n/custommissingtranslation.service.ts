import { Injectable } from '@angular/core';
import {MissingTranslationHandler, MissingTranslationHandlerParams} from "@ngx-translate/core";

@Injectable({
  providedIn: 'root'
})
export class CustomMissingTranslationService implements MissingTranslationHandler {

  constructor() { }

  handle(params: MissingTranslationHandlerParams): any {
    return this.getMissingTranslationLabel(params.translateService.currentLang);
  }

  public getMissingTranslationLabel(lang: string) : string {
    switch (lang) {
      case "de":
        return "Übersetzung ist nicht verfügbar";
      case "fr":
        return "La traduction est indisponible";
      case "pl":
        return "Tłumaczenie jest niedostępne";
      default:
        return "Translation is unavailable";
    }
  }
}
