import {CommentsComponent} from "./comments.component";
import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {AppConfigService, AppsService} from "../../service";
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {Observable} from "rxjs";
import {Id} from "../../model";

describe('CommentComponent',()=>{
   let component:CommentsComponent;
   let fixture:ComponentFixture<CommentsComponent>;
   let appConfigService:AppConfigService;
   let appsService:AppsService;
   let spy:any;
   let spy2:any;
   let spy3:any;

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
       spy = spyOn(appConfigService, 'getApiUrl').and.returnValue("http://localhost/api/");
       spy2 = spyOn(appsService, 'getAppCommentsByUrl').and.returnValue(Observable.of([]));
       fixture.detectChanges();
   });

   it('should create app', ()=>{
       let app = fixture.debugElement.componentInstance;
       expect(app).toBeTruthy();
   });

   it('should refresh', ()=>{
      component.refresh();
      expect(appsService.getAppCommentsByUrl).toHaveBeenCalledTimes(2);
   });

   it('should not add comment', ()=>{
       spy3 = spyOn(appsService, 'addAppCommentByUrl').and.callThrough();
       component.addComment();
       expect(appsService.addAppCommentByUrl).not.toHaveBeenCalled();
   });

   it('should add comment', ()=>{
       spy3 = spyOn(appsService, 'addAppCommentByUrl').and.returnValue(Observable.of(new Id(6)));
       component.newComment.comment = "New";
       component.addComment();
       expect(appsService.addAppCommentByUrl).toHaveBeenCalled();
   });//tu

   it('should not add reply', ()=>{
       spy3 = spyOn(appsService, 'addAppCommentByUrl').and.callThrough();
       component.addReply(1);
       expect(appsService.addAppCommentByUrl).not.toHaveBeenCalled();
   });

   it('should add reply', ()=>{
       spy3 = spyOn(appsService, 'addAppCommentByUrl').and.returnValue(Observable.of(new Id(7)));
       component.newReply.comment = "test";
       component.addReply(7);
       expect(appsService.addAppCommentByUrl).toHaveBeenCalled();
   });

   it('should delete comment', ()=>{
      spy3 = spyOn(appsService, 'deleteAppCommentByUrl').and.returnValue(Observable.of());
      component.deleteComment(1);
      expect(appsService.deleteAppCommentByUrl).toHaveBeenCalled();
   });//tu

   it('should set properties', ()=>{
      component.setCommentNumberOnClick(1, false);
      expect(component.commentId).toBe(1);
      expect(component.subComment).toBe(false);
      expect(component.newReply.comment).toBe("");
      expect(component.active).toBe(true);
      expect(component.replyErrorMsg).toBeUndefined();
   });

});