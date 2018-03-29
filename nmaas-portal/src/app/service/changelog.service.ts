import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ChangelogService {

	constructor(private http: HttpClient) { }

	getChangelog() {
		return this.http.get('http://localhost:4200/assets/changelog/changelog.json');
	}

}
