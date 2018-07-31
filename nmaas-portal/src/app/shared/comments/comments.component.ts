import {Component, Input, OnInit, ViewEncapsulation} from '@angular/core';

import {AppsService} from '../../service/index';
import {Comment, Id} from '../../model/index';
import {AuthService} from '../../auth/auth.service';

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

    newReply: Comment = new Comment();

    comments: Comment[] = [];

    commentId: number;

    subComment: boolean;

    active:boolean = false;

    constructor(private appsService: AppsService, private authService:AuthService) { }

    ngOnInit() {
        this.refresh();
    }

    public refresh(): void {
        this.appsService.getAppCommentsByUrl(this.pathUrl).subscribe(comments => this.comments = comments);
    }

    public addComment(): void {
        this.appsService.addAppCommentByUrl(this.pathUrl, this.newComment)
                        .subscribe(id => {
                            this.newComment = new Comment();
                            this.refresh();
                            });

    }

    public addReply(parentId:number):void{
        this.newReply.parentId = parentId;
        this.appsService.addAppCommentByUrl(this.pathUrl, this.newReply)
            .subscribe(id => {
                this.newReply = new Comment();
                this.refresh();
            });
        this.active = false;
    }

    public deleteComment(id: Number): void {
        this.appsService.deleteAppCommentByUrl(this.pathUrl, new Id(id)).subscribe(
                () => { this.refresh() }
            );
    }

    public setCommentNumberOnClick(commentId:number, subComment:boolean){
        this.commentId = commentId;
        this.subComment = subComment;
        this.newReply.comment = "";
        this.active = true;
    }
}
