/* tslint:disable:no-unused-variable */
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {Pipe, PipeTransform} from '@angular/core';

import {AppInstanceProgressComponent} from './appinstanceprogress.component';
import {TranslateService} from '@ngx-translate/core';
import {AppInstanceProgressStage, AppInstanceState} from '../../../model';

@Pipe({
  name: 'translate'
})
class TranslatePipeMock implements PipeTransform {
  public name = 'translate';

  public transform(query: string, ...args: any[]): any {
    return query;
  }
}

class MockTranslateService {
  public instant(key: string): string {
    return key;
  }
}

describe('AppInstanceProgressComponent', () => {
  let component: AppInstanceProgressComponent;
  let fixture: ComponentFixture<AppInstanceProgressComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppInstanceProgressComponent, TranslatePipeMock ],
      providers: [
        {provide: TranslateService, useClass: MockTranslateService},
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppInstanceProgressComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // disable test until the issue is resolved
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('default stage list should be length 0', () => {
    expect(component.stages.length).toEqual(0);
  });

  it('translate tag function should return', () => {
    const key = 'TEXT';
    expect(component.getTranslateTag(key)).toEqual('APP_INSTANCE.PROGRESS.' + key);
  });

  it('should display with given stages', () => {
    const stages = new Array<AppInstanceProgressStage>();
    stages.push(new AppInstanceProgressStage('Stage0', AppInstanceState.PREPARATION));
    stages.push(new AppInstanceProgressStage('Stage1', AppInstanceState.CONFIGURATION_AWAITING));
    stages.push(new AppInstanceProgressStage('Stage2', AppInstanceState.DEPLOYING));
    stages.push(new AppInstanceProgressStage('Stage3', AppInstanceState.RUNNING));
    stages.push(new AppInstanceProgressStage('Stage4', AppInstanceState.DONE));
    component.stages = stages;
    component.activeState = AppInstanceState.RUNNING;
    component.previousState = AppInstanceState.DEPLOYING;
    expect(component.stages.length).toEqual(5);
    fixture.detectChanges()
    // TODO finish
  });
});
