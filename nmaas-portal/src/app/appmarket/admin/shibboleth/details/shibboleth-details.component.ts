import {Component, OnInit} from '@angular/core';
import {BaseComponent} from "../../../../shared/common/basecomponent/base.component";
import {ShibbolethService} from "../../../../service/shibboleth.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ShibbolethConfig} from "../../../../model/shibboleth";
import {ComponentMode} from "../../../../shared";

@Component({
  selector: 'app-shibbolethdetails',
  templateUrl: './shibboleth-details.component.html',
  styleUrls: ['./shibboleth-details.component.css']
})
export class ShibbolethDetailsComponent extends BaseComponent implements OnInit {

  private config_id:number;
  private shibbolethConfig:ShibbolethConfig;

  constructor(private shibbolethService:ShibbolethService, private route:ActivatedRoute, private router:Router) {
    super();
  }

  ngOnInit() {
    this.shibbolethService.getAll().subscribe(config =>{
      if(config.length > 0){
        this.shibbolethConfig = config[0];
        this.config_id = this.shibbolethConfig.id;
        this.router.navigate(["/admin/shibboleth", this.config_id]);
      } else{
        this.shibbolethConfig = new ShibbolethConfig();
        this.mode = ComponentMode.CREATE;
      }
    });
  }

  public onDelete($event){
    this.shibbolethService.remove($event).subscribe(response => this.router.navigate(['/admin/shibboleth']));
  }

  public onSave($event){
    const newConfig = $event;

    if(!newConfig){
      return;
    }
    if(newConfig.id){
      this.shibbolethService.update(newConfig).subscribe(e=>this.router.navigate(['/admin/shibboleth']));
    } else{
      newConfig.id = this.config_id;
      this.shibbolethService.add(newConfig).subscribe(id=>this.router.navigate(['/admin/shibboleth/', id]));
    }
  }

}
