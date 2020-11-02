import {AppInstanceProgressStage} from './app-instance-progress-stage';
import {AppInstanceState} from './app-instance-status';

describe('AppInstanceProgressStage', () => {

    it('should create an instance', () => {
        expect(new AppInstanceProgressStage('name', AppInstanceState.RUNNING)).toBeTruthy();
    });

    it('check app instance progress stage visibility', () => {
        const stage1 = new AppInstanceProgressStage('test', AppInstanceState.RUNNING)
        expect(stage1.isVisible(AppInstanceState.DEPLOYING)).toEqual(true);
        expect(stage1.isVisible(null)).toEqual(true);  // why?

        const stage2 = new AppInstanceProgressStage('Removed', AppInstanceState.DONE, [AppInstanceState.UNDEPLOYING, AppInstanceState.DONE])
        expect(stage2.isVisible(AppInstanceState.RUNNING)).toEqual(false);
        expect(stage2.isVisible(AppInstanceState.UNDEPLOYING)).toEqual(true);
        expect(stage2.isVisible(AppInstanceState.DONE)).toEqual(true);

        expect(stage2.isVisible(null)).toEqual(true); // why?
    });
});
