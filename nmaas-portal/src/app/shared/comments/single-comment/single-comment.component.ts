import {Component, Input, OnInit, Output, EventEmitter} from '@angular/core';
import {AuthService} from "../../../auth/auth.service";
import {TranslateService} from "@ngx-translate/core";
import {formatDate} from "@angular/common";
import {Comment, Id} from "../../../model";
import {AppsService} from "../../../service";
import {isNullOrUndefined} from "util";
import {findIndex} from "rxjs/operators";

@Component({
  selector: 'app-single-comment',
  templateUrl: './single-comment.component.html',
  styleUrls: ['./single-comment.component.css']
})
export class SingleCommentComponent implements OnInit {

  @Input()
  private pathUrl: string;

  @Input()
  public commentText: string;

  @Input()
  public commentAuthor: string;

  @Input()
  public commentId: number;

  @Input()
  public parentId: number;

  @Input()
  public createdAt: string;

  @Output()
  removeEvent = new EventEmitter<number>();

  @Output()
  addReplyEvent = new EventEmitter<object>();

  public replyBoxVisible: boolean = false;

  constructor(public appsService: AppsService, public authService: AuthService, public translate: TranslateService) { }

  ngOnInit() {
    this.translateCommentText();
  }

  public translateCommentText(){
    if(!isNullOrUndefined(this.commentText)) {
      let startPosition = this.commentText.toString().indexOf("@@@\'");
      if (startPosition > -1) {
        let endPosition = this.commentText.toString().indexOf("\'", startPosition + 4);
        let key = this.commentText.slice(startPosition, endPosition).replace('@@@\'', '');
        let translation = "";
        this.translate.get(key).subscribe((str: string) => {
          translation = str;
        });
        this.commentText = this.commentText.slice(0, startPosition) + translation + this.commentText.slice(endPosition + 1, this.commentText.length);
      }
    }
  }

  public getParsedCommentDate(): string{
    let actual_date = Date.now();
    let comment_date = new Date(this.createdAt);
    let time = actual_date - comment_date.getTime();
    let days = (time / (60*60*24*1000));
    let x = "";
    if(days< 1){
      this.translate.get('COMMENTS.COMMENTS_TODAY').subscribe((res: string) => {
        x = res + formatDate(comment_date, 'h:mm a', 'en-GB');
      });
      return x;
    }else{
      if(days < 2){
        this.translate.get('COMMENTS.COMMENTS_YESTERDAY').subscribe((res: string)=> {
          x = res + formatDate(comment_date, 'h:mm a', 'en-GB');
        });
        return x;
      }else{
        return formatDate(comment_date, 'MMMM d, y, h:mm:ss a z', 'en-GB');
      }
    }
  }

  public deleteComment(id: number): void {
    this.removeEvent.emit(id);
  }

  public addReplyToComment(id: number, text: string){
    if(!isNullOrUndefined(this.parentId)){
      text = "<span class='text-muted'><em>@@@\'COMMENTS.RESPONSE_TO\' \@" + this.commentAuthor + " </em></span></br>" + text;
      id = this.parentId;
    }
    this.addReplyEvent.emit({'id': id, 'text': text});
  }

  public changeReplyBoxVisibility(){
    this.replyBoxVisible = !this.replyBoxVisible;
  }

}
