import {Component, Input, OnInit, ViewEncapsulation} from '@angular/core';

import {AppsService} from '../../service/index';
import {Comment, Id} from '../../model/index';
import {isNullOrUndefined} from "util";
import {AuthService} from "../../auth/auth.service";
import {TranslateService} from '@ngx-translate/core';

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

    replyErrorMsg: string;

    commentErrorMsg: string;

    constructor(private appsService: AppsService, private authService: AuthService, private translate: TranslateService) {
    }

    ngOnInit() {
        this.refresh();
    }

    public refresh(): void {
        this.appsService.getAppCommentsByUrl(this.pathUrl).subscribe(comments => this.comments = comments);
    }

    public addComment(): void {
        if(isNullOrUndefined(this.newComment.comment) || this.newComment.comment === ''){
            this.commentErrorMsg = this.translate.instant('SUBSCRIPTION.COMMENTS_NOT_EMPTY_MESSAGE');
        } else{
            this.appsService.addAppCommentByUrl(this.pathUrl, this.newComment)
                .subscribe(id => {
                    this.newComment = new Comment();
                    this.commentErrorMsg = undefined;
                    this.refresh();
                }, err=> this.commentErrorMsg = err.message);
        }
    }

    public addReply(parentId:number):void{
        if(isNullOrUndefined(this.newReply.comment) || this.newReply.comment === '') {
            this.replyErrorMsg = this.translate.instant('SUBSCRIPTION.COMMENTS_NOT_EMPTY_MESSAGE');
        } else{
            this.newReply.parentId = parentId;
            this.appsService.addAppCommentByUrl(this.pathUrl, this.newReply)
                .subscribe(id => {
                    this.newReply = new Comment();
                    this.refresh();
                    this.active = false;
                }, err=> this.replyErrorMsg = err.message);
        }
    }

    private addReplyBasedOnEvent(parentId: number, text: string){
        if(isNullOrUndefined(text) || text === ''){
            this.commentErrorMsg = this.translate.instant('SUBSCRIPTION.COMMENTS_NOT_EMPTY_MESSAGE');
        } else{
            this.newComment = new Comment();
            this.newComment.comment = text;
            this.newComment.parentId = parentId;
            this.appsService.addAppCommentByUrl(this.pathUrl, this.newComment)
              .subscribe(id => {
                  this.newComment = new Comment();
                  this.commentErrorMsg = undefined;
                  this.refresh();
              }, err=> this.commentErrorMsg = err.message);
        }
    }

    public deleteComment(id: number): void {
        this.appsService.deleteAppCommentByUrl(this.pathUrl, new Id(id)).subscribe(
                () => { this.refresh() }
            );
    }

    OnRemove($event){
        console.debug("OnRemove catches event with id: " + $event);
        this.deleteComment($event);
    }

    onAddReply($event){
        console.debug("OnAddReply catches event with id: " + $event['id'] + " and string: " + $event['text']);
        this.addReplyBasedOnEvent($event['id'], $event['text']);
    }

    public setCommentNumberOnClick(commentId:number, subComment:boolean){
        this.commentId = commentId;
        this.subComment = subComment;
        this.newReply.comment = "";
        this.active = true;
        this.replyErrorMsg = undefined;
    }
}
