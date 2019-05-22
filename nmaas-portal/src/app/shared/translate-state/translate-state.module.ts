import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TranslateService} from "@ngx-translate/core";

@NgModule({
  declarations: [],
  imports: [
    CommonModule
  ]
})
export class TranslateStateModule {

  constructor(private translate: TranslateService){}

  public translateState(appState): string{
    let outputString = "";
    console.debug("CHECKING ENUM: " + "ENUM." + appState.toString());
    this.translate.get("ENUM." + appState.toString()).subscribe((res: string) => {
      outputString = res;
    });
    return outputString;
    }

}
