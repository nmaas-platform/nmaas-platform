import {CommentsComponent} from "./comments.component";
import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {AppConfigService, AppsService} from "../../service";
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";

describe('CommentComponent',()=>{
   let component:CommentsComponent;
   let fixture:ComponentFixture<CommentsComponent>;
   let appConfigService:AppConfigService;
   let appsService:AppsService;
   let spy:any;

   beforeEach(async (()=>{
       TestBed.configureTestingModule({
          declarations: [CommentsComponent],
          imports:[
              FormsModule,
              HttpClientModule,
              TranslateModule.forRoot({
                  loader: {
                      provide: TranslateLoader,
                      useClass: TranslateFakeLoader
                  }
              })
          ],
          providers: [AppsService, AppConfigService]
       }).compileComponents();
   }));

   beforeEach(()=>{
       fixture = TestBed.createComponent(CommentsComponent);
       component = fixture.componentInstance;
       appConfigService = fixture.debugElement.injector.get(AppConfigService);
       appsService = fixture.debugElement.injector.get(AppsService);
       spy = spyOn(appConfigService, 'getApiUrl').and.returnValue("http://localhost/api");
       fixture.detectChanges();
   });

   it('should create app', ()=>{
       let app = fixture.debugElement.componentInstance;
       expect(app).toBeTruthy();
   });

   it('should refresh', ()=>{
      spy = spyOn(appsService, 'getAppCommentsByUrl').and.callThrough();
      component.refresh();
      expect(appsService.getAppCommentsByUrl).toHaveBeenCalled();
   });

   it('should not add comment', ()=>{
       spy = spyOn(appsService, 'addAppCommentByUrl').and.callThrough();
       component.addComment();
       expect(appsService.addAppCommentByUrl).not.toHaveBeenCalled();
   });

   it('should add comment', ()=>{
       spy = spyOn(appsService, 'addAppCommentByUrl').and.callThrough();
       component.newComment.comment = "New";
       component.addComment();
       expect(appsService.addAppCommentByUrl).toHaveBeenCalled();
   });

   it('should not add reply', ()=>{
       spy = spyOn(appsService, 'addAppCommentByUrl').and.callThrough();
       component.addReply(1);
       expect(appsService.addAppCommentByUrl).not.toHaveBeenCalled();
   });

   it('should add reply', ()=>{
       spy = spyOn(appsService, 'addAppCommentByUrl').and.callThrough();
       component.newReply.comment = "test";
       component.addReply(1);
       expect(appsService.addAppCommentByUrl).toHaveBeenCalled();
       expect(component.newReply.parentId).toBe(1);
   });

   it('should delete comment', ()=>{
      spy = spyOn(appsService, 'deleteAppCommentByUrl').and.callThrough();
      component.deleteComment(1);
      expect(appsService.deleteAppCommentByUrl).toHaveBeenCalled();
   });

   it('should set properties', ()=>{
      component.setCommentNumberOnClick(1, false);
      expect(component.commentId).toBe(1);
      expect(component.subComment).toBe(false);
      expect(component.newReply.comment).toBe("");
      expect(component.active).toBe(true);
      expect(component.replyErrorMsg).toBeUndefined();
   });

});