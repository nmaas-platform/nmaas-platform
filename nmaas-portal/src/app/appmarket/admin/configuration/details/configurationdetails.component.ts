import {Component, OnInit} from '@angular/core';
import {BaseComponent} from '../../../../shared/common/basecomponent/base.component';
import {Router} from '@angular/router';
import {ConfigurationService} from '../../../../service';
import {Configuration} from '../../../../model/configuration';

@Component({
  selector: 'app-configurationdetails',
  templateUrl: './configurationdetails.component.html',
  styleUrls: ['./configurationdetails.component.css']
})
export class ConfigurationDetailsComponent extends BaseComponent implements OnInit {

  public configuration:Configuration;

  constructor(private router:Router, private configurationService:ConfigurationService) {
    super();
  }

  ngOnInit() {
      this.update();
  }

  public update():void{
      this.configurationService.getConfiguration().subscribe(value => this.configuration = value);
  }

  public save():void{
      this.configurationService.updateConfiguration(this.configuration).subscribe(() => this.update());
  }


}
