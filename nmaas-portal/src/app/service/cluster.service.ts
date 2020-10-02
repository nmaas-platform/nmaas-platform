import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {GenericDataService} from './genericdata.service';

import {HttpClient} from '@angular/common/http'
import {AppConfigService} from './appconfig.service';

import {Cluster} from '../model/cluster';

@Injectable()
export class ClusterService extends GenericDataService {

    protected url: string;

    constructor(http: HttpClient, appConfig: AppConfigService) {
        super(http, appConfig);

        this.url = this.appConfig.getApiUrl() + '/management/kubernetes/';
    }

    public getCluster(): Observable<Cluster> {
        return this.get<Cluster>(this.url);
    }

    public add(cluster: Cluster): Observable<any> {
        return this.post(this.url, cluster);
    }

    public update(cluster: Cluster): Observable<any> {
        return this.put(this.url + cluster.id, cluster);
    }

    public remove(clusterId: number): Observable<any> {
        return this.http.delete(this.url + clusterId);
    }
}
