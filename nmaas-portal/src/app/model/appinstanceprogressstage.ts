import { AppInstanceState } from './appinstancestatus';

export class AppInstanceProgressStage {
   constructor(public name:string, public activeState: AppInstanceState, public visibleWhen?: AppInstanceState[]) {   
   }
    
    public isVisible(appInstanceState : AppInstanceState):boolean {
       return (!this.visibleWhen || this.visibleWhen.filter(state => state === appInstanceState).length > 0);
    }
}