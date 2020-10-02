import { Injectable } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr'
import localeDe from '@angular/common/locales/de'
import localePl from '@angular/common/locales/pl'
import localeEn from '@angular/common/locales/en-GB'

@Injectable({ providedIn: 'root' })
export class SessionService {

  private _locale: string;

  set locale(value: string) {
    this._locale = value;
  }
  get locale(): string {
    return this._locale || 'en-GB';
  }

  registerCulture(culture: string) {
    console.debug("Lang: ", culture);
    if (!culture) {
      return;
    }
    this.locale = culture;
    switch (culture) {
      case 'fr': {
        registerLocaleData(localeFr);
        break;
      }
      case 'de': {
        registerLocaleData(localeDe);
        break;
      }
      case 'pl': {
        registerLocaleData(localePl);
        break;
      }
      default: {
        registerLocaleData(localeEn);
        break;
      }
    }
  }
}
