import { Component, OnInit, Input, ViewEncapsulation } from '@angular/core';

import { AppsService } from '../../service/index';
import { Id, Comment } from '../../model/index';

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

    newComment: Comment = new Comment();

    comments: Comment[];

    constructor(private appsService: AppsService) { }

    ngOnInit() {
        this.refresh();
    }

    public refresh() : void {
        this.appsService.getAppCommentsByUrl(this.pathUrl).subscribe(comments => this.comments = comments);
    }
    
    public addComment(): void {
        this.appsService.addAppCommentByUrl(this.pathUrl, this.newComment)
                        .subscribe(id => { 
                            this.newComment = new Comment();
                            this.refresh();
                            });
                          
    }

    public deleteComment(id: Number): void {
        this.appsService.deleteAppCommentByUrl(this.pathUrl, new Id(id)).subscribe(
                () => { this.refresh() }
            );
    }
}
