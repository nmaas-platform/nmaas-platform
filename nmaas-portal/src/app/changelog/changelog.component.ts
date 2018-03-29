import { Component, OnInit } from '@angular/core';

import { Router } from '@angular/router';
import { NavbarComponent } from '../shared/index';
import { FooterComponent } from '../shared/index';
import { ChangelogService } from '../service/index';

@Component({
	selector: 'app-changelog',
	templateUrl: './changelog.component.html',
	styleUrls: ['./changelog.component.css']
})
export class ChangelogComponent implements OnInit {

	changelog: any;
	test:any = {
		arr:[
		{
			a: "Hello"
		},
		{
			b: "whatsup"
		}]
	}

	constructor(private router: Router, private changelogService: ChangelogService) { }

	ngOnInit() {
		this.changelogService.getChangelog().subscribe(changelog => {
			this.changelog = changelog;
			console.log(this.changelog);
			console.log(this.test);
		})
	}

	public isRouteLogin(): boolean {
		return this.router.url === '/login'? true : false;
	}
}
