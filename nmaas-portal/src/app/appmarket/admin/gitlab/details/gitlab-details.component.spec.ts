import { async, ComponentFixture, TestBed } from '@angular/core/testing';

class MockGitlabService{
    protected url:string;

    constructor() {
        this.url = 'http://localhost/api';
    }

    public getAll():Observable<GitLabConfig[]>{
        return Observable.of<GitLabConfig[]>();
    }

    public getOne(config_id:number):Observable<GitLabConfig>{
        return Observable.of<GitLabConfig>();
    }

    public add(gitLabConfig:GitLabConfig):Observable<any>{
        return Observable.of<GitLabConfig>();
    }

    public update(gitLabConfig:GitLabConfig):Observable<any>{
        return Observable.of<GitLabConfig>();
    }

    public remove(config_id:number):Observable<any>{
        return Observable.of<GitLabConfig>();
    }
}

export class MockGitLabConfig{
    public id: number;
    public server:string;
    public sshServer:string;
    public token:string;
    public port:number;
    public repositoryAccessUsername:string = "nmaas-conf-automation";
}

//import { GitlabDetailsComponent } from './gitlab-details.component';
import { GitlabDetailsComponent } from '../../../../shared/admin/gitlab/details/gitlab-details.component'
import {FormsModule} from "@angular/forms";
import {RouterTestingModule} from "@angular/router/testing";
import {AppConfigService, ConfigurationService} from "../../../../service";
import {HttpClient, HttpHandler} from "@angular/common/http";
import {GitlabService} from "../../../../service/gitlab.service";
import {Observable} from "rxjs";
import {GitLabConfig} from "../../../../model/gitlab";

describe('GitlabDetailsComponent', () => {
  let component: GitlabDetailsComponent;
  let fixture: ComponentFixture<GitlabDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
        declarations: [ GitlabDetailsComponent ],
        imports: [
            FormsModule,
            RouterTestingModule,
        ],
        providers: [
            {provide: GitlabService, useClass: MockGitlabService},
            {provide: GitLabConfig, useClass: MockGitLabConfig},
            HttpClient,
            HttpHandler,
            AppConfigService,

        ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GitlabDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    let app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });
});
