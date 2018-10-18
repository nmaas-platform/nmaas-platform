import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {GenericDataService} from "./genericdata.service";
import {AppConfigService} from "./appconfig.service";
import {GitInfo} from "../model/gitinfo";

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
		return this.get<GitInfo>(this.url+'/git');
	}
}
