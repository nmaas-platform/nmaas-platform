import {CommentsComponent} from "./comments.component";
import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {AppConfigService, AppsService} from "../../service";
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {Id} from "../../model";
import {AuthService} from "../../auth/auth.service";
import {JwtHelperService, JwtModule} from "@auth0/angular-jwt";

describe('CommentComponent',()=>{
   let component:CommentsComponent;
   let fixture:ComponentFixture<CommentsComponent>;
   let appConfigService:AppConfigService;
   let appsService:AppsService;
   let authService:AuthService;

   beforeEach(async (()=>{
       TestBed.configureTestingModule({
          declarations: [CommentsComponent],
          imports:[
              FormsModule,
              HttpClientModule,
              JwtModule.forRoot({}),
              TranslateModule.forRoot({
                  loader: {
                      provide: TranslateLoader,
                      useClass: TranslateFakeLoader
                  }
              })
          ],
          providers: [AppsService, AuthService, AppConfigService]
       }).compileComponents();
   }));

   beforeEach(()=>{
       fixture = TestBed.createComponent(CommentsComponent);
       component = fixture.componentInstance;
       appConfigService = fixture.debugElement.injector.get(AppConfigService);
       appsService = fixture.debugElement.injector.get(AppsService);
       authService = fixture.debugElement.injector.get(AuthService);
       spyOn(appConfigService, 'getApiUrl').and.returnValue("http://localhost/api/");
       spyOn(appsService, 'getAppCommentsByUrl').and.returnValue(of([]));
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
       spyOn(appsService, 'addAppCommentByUrl').and.callThrough();
       component.addComment();
       expect(appsService.addAppCommentByUrl).not.toHaveBeenCalled();
   });

   it('should add comment', ()=>{
       spyOn(appsService, 'addAppCommentByUrl').and.returnValue(of(new Id(6)));
       component.newComment.comment = "New";
       component.addComment();
       expect(appsService.addAppCommentByUrl).toHaveBeenCalled();
   });

   it('should not add reply', ()=>{
       spyOn(appsService, 'addAppCommentByUrl').and.callThrough();
       component.addReply(1);
       expect(appsService.addAppCommentByUrl).not.toHaveBeenCalled();
   });

   it('should add reply', ()=>{
       spyOn(appsService, 'addAppCommentByUrl').and.returnValue(of(new Id(7)));
       component.newReply.comment = "test";
       component.addReply(7);
       expect(appsService.addAppCommentByUrl).toHaveBeenCalled();
   });

   it('should delete comment', ()=>{
      spyOn(appsService, 'deleteAppCommentByUrl').and.returnValue(of());
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
