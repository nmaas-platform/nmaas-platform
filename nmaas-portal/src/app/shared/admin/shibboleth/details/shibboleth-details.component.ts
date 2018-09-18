import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ShibbolethConfig} from "../../../../model/shibboleth";
import {Router} from "@angular/router";
import {BaseComponent} from "../../../common/basecomponent/base.component";
import {ComponentMode} from "../../../common/componentmode";

@Component({
  selector: 'nmaas-shibbolethdetails',
  templateUrl: './shibboleth-details.component.html',
  styleUrls: ['./shibboleth-details.component.css']
})
export class ShibbolethDetailsComponent extends BaseComponent implements OnInit {

  @Input()
  shibbolethConfig:ShibbolethConfig = new ShibbolethConfig();

  @Output()
  onSave:EventEmitter<ShibbolethConfig> = new EventEmitter<ShibbolethConfig>();

  @Output()
  onDelete:EventEmitter<number> = new EventEmitter<number>();

  @Input()
  private errMsg: string;

  constructor(private router:Router) { super(); }

  ngOnInit() {
  }

  public onModeChange():void{
      const newMode: ComponentMode = (this.mode === ComponentMode.VIEW ? ComponentMode.EDIT : ComponentMode.VIEW);
      if (this.isModeAllowed(newMode)) {
          this.mode = newMode;
          if(this.mode === ComponentMode.VIEW){
              this.router.navigate(['admin/shibboleth'])
          }
      }
  }

  public submit(){
    this.onSave.emit(this.shibbolethConfig);
  }

  public remove(){
    this.onDelete.emit(this.shibbolethConfig.id);
  }

}
