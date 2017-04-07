import { Component, OnInit, Input, ViewEncapsulation } from '@angular/core';

import { AppsService } from '../../service/index';
import { Comment } from '../../model/index';

@Component({
    selector: 'comments',
    templateUrl: './comments.component.html',
    styleUrls: ['./comments.component.css'],
    encapsulation: ViewEncapsulation.None,
    providers: [AppsService]
})
export class CommentsComponent implements OnInit {

    @Input()
    private pathUrl: string;

    comments: Comment[];

    constructor(private appsService: AppsService) { }

    ngOnInit() {
        this.appsService.getAppCommentsByUrl(this.pathUrl).subscribe(comments => this.comments = comments);
    }

}
