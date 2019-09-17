import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { SingleCommentComponent } from './single-comment.component';
import {AppConfigService, AppsService} from "../../../service";
import {AuthService} from "../../../auth/auth.service";
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {JwtModule} from "@auth0/angular-jwt";
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {of} from "rxjs";


describe('SingleCommentComponent', () => {
  let component: SingleCommentComponent;
  let fixture: ComponentFixture<SingleCommentComponent>;
  let appConfigService:AppConfigService;
  let appsService:AppsService;
  let authService:AuthService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SingleCommentComponent ],
      imports: [
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
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SingleCommentComponent);
    component = fixture.componentInstance;
    appConfigService = fixture.debugElement.injector.get(AppConfigService);
    appsService = fixture.debugElement.injector.get(AppsService);
    authService = fixture.debugElement.injector.get(AuthService);
    spyOn(appConfigService, 'getApiUrl').and.returnValue("http://localhost/api/");
    spyOn(authService, 'hasRole').and.returnValue(true);
    spyOn(appsService, 'getAppCommentsByUrl').and.returnValue(of([]));
    let dte = new Date();
    dte.setDate(dte.getDate() - 2);
    component.createdAt = dte.toString();
    fixture.detectChanges();
  });

  it('should create', () => {
    let app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should change visibility of changeReplyBox', () => {
    let actualState = component.replyBoxVisible;
    component.changeReplyBoxVisibility();
    expect(component.replyBoxVisible != actualState).toBeTruthy();
  });

  it('should emit delete event', () => {
    spyOn(component.removeEvent, 'emit');
    // trigger event
    component.deleteComment(1);
    fixture.detectChanges();
    expect(component.removeEvent.emit).toHaveBeenCalledWith(1);
  });

  it('should emit add reply event', () => {
    spyOn(component.addReplyEvent, 'emit');
    // trigger event
    component.addReplyToComment(1, 'test');
    fixture.detectChanges();
    expect(component.addReplyEvent.emit).toHaveBeenCalledWith({ 'id': 1, 'text': 'test'});
  });

  it('getParsedCommentData should contain today', () => {
    let dte = new Date();
    dte.setDate(dte.getDate());
    component.createdAt = dte.toString();
    fixture.detectChanges();
    let out = component.getParsedCommentDate().toLowerCase();
    expect(out).toContain("today");
  });

  it('getParsedCommentData should contain yesterday', () => {
    let dte = new Date();
    dte.setDate(dte.getDate() - 1);
    component.createdAt = dte.toString();
    fixture.detectChanges();
    let out = component.getParsedCommentDate().toLowerCase();
    expect(out).toContain("yesterday");
  });

});
