import {Component, Input, OnInit, Output, EventEmitter} from '@angular/core';
import {AuthService} from "../../../auth/auth.service";
import {TranslateService} from "@ngx-translate/core";
import {formatDate} from "@angular/common";
import {Comment, Id} from "../../../model";
import {AppsService} from "../../../service";

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
  public parentId: string;

  @Input()
  public createdAt: string;

  @Output()
  removeEvent = new EventEmitter<number>();

  @Output()
  addReplyEvent = new EventEmitter<object>();

  public replyBoxVisible: boolean = false;

  constructor(private appsService: AppsService, private authService: AuthService, private translate: TranslateService) { }

  ngOnInit() {
  }

  public getParsedCommentDate(): string{
    let actual_date = Date.now();
    let comment_date = new Date(this.createdAt);
    let time = actual_date - comment_date.getTime();
    let days = (time / (60*60*24*1000));
    if(days< 1){
      return "Today at " + formatDate(comment_date, 'h:mm a', 'en-GB');
    }else{
      if(days < 2){
        return "Yesterday at " + formatDate(comment_date, 'h:mm a', 'en-GB');
      }else{
        return formatDate(comment_date, 'MMMM d, y, h:mm:ss a z', 'en-GB');
      }
    }
  }

  public deleteComment(id: number): void {
    console.debug("Delete comment emits: " + id);
    this.removeEvent.emit(id);
  }

  public addReplyToComment(id: number, text: string){
    console.debug("Add reply emits: " + id);
    this.addReplyEvent.emit({'id': id, 'text': text});
  }

  public changeReplyBoxVisibility(){
    this.replyBoxVisible = !this.replyBoxVisible;
    console.debug("Reply visibility changed to: " + this.replyBoxVisible)
  }

}
