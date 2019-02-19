import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {ModalComponent} from "../../../shared/modal";
import {ApplicationState} from "../../../model/applicationstate";
import {AppsService} from "../../../service";
import {Application} from "../../../model";

@Component({
  selector: 'app-appchangestatemodal',
  templateUrl: './appchangestatemodal.component.html',
  styleUrls: ['./appchangestatemodal.component.css']
})
export class AppChangeStateModalComponent implements OnInit, OnChanges {

  @ViewChild(ModalComponent)
  public readonly modal: ModalComponent;

  @Input()
  public app: Application;

  public selectedState: ApplicationState = undefined;

  public stateList: ApplicationState[] = [];

  constructor(public appsService: AppsService) {}

  ngOnInit() {
    this.filterStates();
    this.selectedState = this.stateList[0];
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.filterStates();
    this.selectedState = this.stateList[0];
  }

  private filterStates() : void {
    switch(this.getStateAsString(this.app.state)){
      case this.getStateAsString(ApplicationState.NEW):
        this.stateList = [ApplicationState.ACTIVE, ApplicationState.REJECTED];
        break;
      case this.getStateAsString(ApplicationState.ACTIVE):
        this.stateList = [ApplicationState.NOT_ACTIVE, ApplicationState.DELETED];
        break;
      case this.getStateAsString(ApplicationState.REJECTED):
        this.stateList = [ApplicationState.NEW, ApplicationState.DELETED];
        break;
      case this.getStateAsString(ApplicationState.NOT_ACTIVE):
        this.stateList = [ApplicationState.ACTIVE, ApplicationState.DELETED];
        break;
      default:
        this.stateList = [];
        break;
    }
  }

  public getStateAsString(state: any): string {
    return typeof state === "string" && isNaN(Number(state.toString())) ? state: ApplicationState[state];
  }

  public submit(): void {
    this.appsService.changeApplicationState(this.app.id, ApplicationState[this.selectedState]).subscribe(()=>{
      console.debug("Application state changed");
      this.app.state = this.selectedState;
      this.selectedState = undefined;
      this.filterStates();
      this.modal.hide();
    });
  }

  public show(): void {
    this.modal.show();
  }

}
