import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ChangelogService {

	constructor(private http: HttpClient) { }

	getChangelog() {
		return this.http.get('assets/changelog/changelog.json');
	}
}
