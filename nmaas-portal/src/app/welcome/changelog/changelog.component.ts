import { Component, OnInit } from '@angular/core';

import { Router } from '@angular/router';
import { NavbarComponent } from '../../shared/index';
import { FooterComponent } from '../../shared/index';
import { ChangelogService } from '../../service/index';

@Component({
	selector: 'app-changelog',
	templateUrl: './changelog.component.html',
	styleUrls: ['./changelog.component.css']
})
export class ChangelogComponent implements OnInit {

	changelog: any;

	constructor(private router: Router, private changelogService: ChangelogService) { }

	ngOnInit() {
		this.changelogService.getChangelog().subscribe((changelog) => {
			this.changelog = changelog;
		})
	}

	public isRouteLogin(): boolean {
		return this.router.url === '/welcome/login' || this.router.url === '/welcome/registration' ? true : false;
	}
}