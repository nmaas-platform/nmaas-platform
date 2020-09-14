import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {ModalComponent} from '../../../shared/modal';
import {ApplicationState} from '../../../model/application-state';
import {AppsService} from '../../../service';
import {AppStateChange} from '../../../model/appstatechange';
import {ApplicationVersion} from '../../../model/application-version';

@Component({
  selector: 'app-appchangestatemodal',
  templateUrl: './appchangestatemodal.component.html',
  styleUrls: ['./appchangestatemodal.component.css']
})
export class AppChangeStateModalComponent implements OnInit, OnChanges {

  @ViewChild(ModalComponent, { static: true })
  public readonly modal: ModalComponent;

  @Input()
  public appName: string;

  @Input()
  public app: ApplicationVersion;

  public stateChange: AppStateChange = new AppStateChange();

  public stateList: ApplicationState[] = [];

  public errorMessage: string;

  constructor(public appsService: AppsService) {}

  ngOnInit() {
    this.filterStates();
    this.stateChange.state = this.stateList[0];
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.filterStates();
    this.stateChange.state = this.stateList[0];
  }

  private filterStates(): void {
    switch (this.getStateAsString(this.app.state)) {
      case this.getStateAsString(ApplicationState.NEW):
        this.stateList = [ApplicationState.ACTIVE, ApplicationState.REJECTED];
        break;
      case this.getStateAsString(ApplicationState.ACTIVE):
        this.stateList = [ApplicationState.DISABLED, ApplicationState.DELETED];
        break;
      case this.getStateAsString(ApplicationState.REJECTED):
        this.stateList = [ApplicationState.NEW, ApplicationState.DELETED];
        break;
      case this.getStateAsString(ApplicationState.DISABLED):
        this.stateList = [ApplicationState.ACTIVE, ApplicationState.DELETED];
        break;
      default:
        this.stateList = [];
        break;
    }
  }

  public getStateAsString(state: string | ApplicationState): string {
    return typeof state === 'string' && isNaN(Number(state.toString())) ? state : ApplicationState[state];
  }

  public submit(): void {
    this.appsService.changeApplicationState(this.app.appVersionId, this.stateChange).subscribe(() => {
      console.debug('Application state changed');
      this.errorMessage = undefined;
      this.app.state = this.stateChange.state;
      this.stateChange.state = undefined;
      this.stateChange.reason = undefined;
      this.filterStates();
      this.modal.hide();
    }, error => this.errorMessage = error.message);
  }

  public show(): void {
    this.modal.show();
  }

}
