import {AppInstanceState, parseAppInstanceState} from './app-instance-status';

export class AppInstanceProgressStage {
   constructor(public name: string, public activeState: AppInstanceState, public visibleWhen?: AppInstanceState[]) {
       this.activeState = parseAppInstanceState(this.activeState); // ensure that app instance state is properly typed
   }

    public isVisible(appInstanceState: AppInstanceState): boolean {
       appInstanceState = parseAppInstanceState(appInstanceState); // ensure that app instance state is properly typed
       return (!this.visibleWhen || appInstanceState == null || this.visibleWhen.filter(state => state === appInstanceState).length > 0);
    }
}
