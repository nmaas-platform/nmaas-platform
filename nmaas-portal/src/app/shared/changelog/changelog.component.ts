import {Component, OnInit} from '@angular/core';

import { Router } from '@angular/router';
import { ChangelogService } from '../../service';

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
}
