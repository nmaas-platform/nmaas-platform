import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { HttpClient } from '@angular/common/http';
import {GenericDataService} from "./genericdata.service";
import {AppConfigService} from "./appconfig.service";

@Injectable()
export class ChangelogService extends GenericDataService{

	protected url:string;

	constructor(http: HttpClient, appConfig: AppConfigService) {
		super(http,appConfig);
		this.url = this.appConfig.getApiUrl()+"/info";
	}

	getChangelog() {
		return this.get<string[]>(this.url+'/changelog');
	}

	getGitInfo(){
		return this.get<string[]>(this.url+'/git');
	}
}
