import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {GenericDataService} from './genericdata.service';

import {HttpClient} from '@angular/common/http'
import {AppConfigService} from './appconfig.service';

import {ClusterInfo, Cluster} from '../model/cluster';

@Injectable()
export class ClusterService extends GenericDataService {

    protected url: string;

    constructor(http: HttpClient, appConfig: AppConfigService) {
        super(http, appConfig);

        this.url = this.appConfig.getApiUrl() + '/management/kubernetes/';
    }

    public getAll(): Observable<ClusterInfo[]> {
        return this.get<ClusterInfo[]>(this.url);
    }

    public getOne(clusterName: string): Observable<Cluster> {
        return this.get<Cluster>(this.url + clusterName);
    }

    public add(cluster: Cluster): Observable<any> {
        return this.post(this.url, cluster);
    }

    public update(cluster: Cluster): Observable<any> {
        return this.put(this.url + cluster.name, cluster);
    }

    public remove(clusterName: string): Observable<any> {
        return this.http.delete(this.url + clusterName);
    }
}