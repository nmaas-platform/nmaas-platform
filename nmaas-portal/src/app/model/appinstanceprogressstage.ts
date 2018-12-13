import { AppInstanceState } from './appinstancestatus';
import {isNullOrUndefined} from "util";

export class AppInstanceProgressStage {
   constructor(public name:string, public activeState: AppInstanceState, public visibleWhen?: AppInstanceState[]) {   
   }
    
    public isVisible(appInstanceState : AppInstanceState):boolean {
       return (!this.visibleWhen || isNullOrUndefined(appInstanceState) || this.visibleWhen.filter(state => AppInstanceState[state].toString() === appInstanceState.toString()).length > 0);
    }
    
}