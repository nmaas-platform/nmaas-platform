import {AppSubscription} from '../../model';
import {AppConfigService, DomainService} from '../../service';
import {AppsService} from '../../service';
import {AppSubscriptionsService} from '../../service/appsubscriptions.service';
import {UserDataService} from '../../service/userdata.service';
import {ListType} from '../common/listtype';
import {AppViewType} from '../common/viewtype';
import {Component, OnInit, Input, OnChanges, SimpleChanges} from '@angular/core';
import {Observable, of} from 'rxjs';
import {Domain} from '../../model/domain';
import {map} from 'rxjs/operators';
import {ApplicationBase} from '../../model/application-base';

function compareAppsName(a: ApplicationBase, b: ApplicationBase): number {
    return a.name.localeCompare(b.name);
}

function compareAppsRating(a: ApplicationBase, b: ApplicationBase): number {
    return (a.rate.averageRate - b.rate.averageRate) * -1; // desc
}

function compareAppsPopularity(a: ApplicationBase, b: ApplicationBase): number {
    return 0; // TODO
}

@Component({
    selector: 'nmaas-applications-view',
    templateUrl: './applications.component.html',
    styleUrls: ['./applications.component.css']
})

export class ApplicationsViewComponent implements OnInit, OnChanges {

    public ListType = ListType;
    public AppViewType = AppViewType;

    @Input()
    public appView: AppViewType = AppViewType.APPLICATION;

    @Input()
    public selectedListType: ListType = ListType.GRID;

    @Input()
    public domainId: number;

    public applications: Observable<ApplicationBase[]>;
    protected copy_applications: Observable<ApplicationBase[]>;
    public selected: Observable<Set<number>>;
    public domain: Observable<Domain>;

    public searchedAppName = '';
    protected searchedTag = 'all';

    public sortModeList = ['NONE', 'NAME', 'RATING', 'POPULAR'];
    public sortMode = 'NONE';

    constructor(private appsService: AppsService,
                private appSubsService: AppSubscriptionsService,
                private userDataService: UserDataService,
                private appConfig: AppConfigService,
                private domainService: DomainService) {
    }

    ngOnInit() {
        // this.updateDomain();
    }

    ngOnChanges(changes: SimpleChanges) {
        console.log('on changes triggered');
        this.updateDomain();
        this.domain = this.domainService.getOne(this.domainId);
    }

    protected updateDomain(): void {
        let domainId: number;
        let applications: Observable<ApplicationBase[]>;

        if ((this.domainId === undefined) || this.domainId === 0 || this.domainId === this.appConfig.getNmaasGlobalDomainId()) {
            domainId = undefined;
        } else {
            domainId = this.domainId;
        }

        switch (+this.appView) {
            case AppViewType.APPLICATION:
                applications = this.appsService.getAllActiveApplicationBase();
                console.log('get apps update domain')
                this.updateSelected();
                // applications.subscribe((apps) => this.updateSelected(apps));
                break;
            case AppViewType.DOMAIN:
                applications = this.appSubsService.getSubscribedApplications(domainId);
                break;
            default:
                applications = of<ApplicationBase[]>([]);
                break;
        }

        this.applications = applications;

    }

    protected updateSelected() {

        let subscriptions: Observable<AppSubscription[]>;
        if (!(this.domainId === undefined || this.domainId === 0 || this.domainId === this.appConfig.getNmaasGlobalDomainId())) {
            subscriptions = this.appSubsService.getAllByDomain(this.domainId);
        }

        if (subscriptions != null) {
            subscriptions.subscribe((appSubs) => {

                const selected: Set<number> = new Set<number>();

                for (let i = 0; i < appSubs.length; i++) {
                    selected.add(appSubs[i].applicationId);
                }

                this.selected = of<Set<number>>(selected);
            });
        } else {
            this.selected = undefined;
        }

    }

    protected doSearch(): void {
        if (this.copy_applications == null) {
            this.copy_applications = this.applications
        }
        this.applications = this.copy_applications;
        const tag = this.searchedTag.toLocaleLowerCase();
        const typed = this.searchedAppName.toLocaleLowerCase();
        this.applications = this.applications.pipe(
            map(apps => {
                    // console.log(apps);
                    let res: ApplicationBase[]
                    if (tag === 'all') { // if all tags than return all
                        res = apps;
                    } else { // filter by tag
                        res = apps.filter(a => a.tags.map(t => t.name.toLocaleLowerCase()).find(t => t.includes(tag)) != null)
                    } // filter by name
                    return res.filter(a => a.name.toLocaleLowerCase().includes(typed));
                }
            ),
            map(apps => {
                switch (this.sortMode) {
                    case 'NAME':
                        return [...apps].sort(compareAppsName)
                    case 'RATING':
                        return [...apps].sort(compareAppsRating)
                    case 'POPULAR':
                        return [...apps].sort(compareAppsPopularity)
                    default:
                        return apps
                }
            })
        );
    }

    public filterAppsByName(typed: string): void {

        this.searchedAppName = typed;
        this.doSearch();
    }

    public filterAppsByTag(tag: string): void {

        this.searchedAppName = '';
        this.searchedTag = tag;
        this.doSearch();
    }

    public onSort(): void {
        this.doSearch();
    }
}
