import {User} from '../../../model';
import {BaseComponent} from '../../common/basecomponent/base.component';
import {
    Component,
    EventEmitter,
    Input,
    OnInit,
    Output,
    ViewChild
} from '@angular/core';
import {AuthService} from '../../../auth/auth.service';
import {PasswordComponent} from '../..';
import {Role} from '../../../model/userrole';

@Component({
    selector: 'nmaas-userdetails',
    templateUrl: './userdetails.component.html',
    styleUrls: ['./userdetails.component.css']
})
export class UserDetailsComponent extends BaseComponent implements OnInit {

    public Role = Role;

    @ViewChild(PasswordComponent, {static: true})
    public readonly passwordModal: PasswordComponent;

    @Input()
    public user: User = new User();

    public _errorMessage: string;

    @Input()
    get errorMessage() {
        return this._errorMessage;
    }

    @Output()
    errorMessageChange: EventEmitter<any> = new EventEmitter();

    set errorMessage(val) {
        this._errorMessage = val;
    }

    @Output()
    public onSave: EventEmitter<User> = new EventEmitter<User>();

    @Output()
    public refresh: EventEmitter<any> = new EventEmitter();

    @Input()
    get userDetailsMode() {
        return this.mode;
    }

    @Output()
    userDetailsModeChange: EventEmitter<any> = new EventEmitter();

    set userDetailsMode(val) {
        this.mode = val;
        this.userDetailsModeChange.emit(this.mode);
    }


    constructor(public authService: AuthService) {
        super();
    }

    ngOnInit() {

    }

    public submit() {
        this.onSave.emit(this.user);
    }

    public onModeChange(): void {
        this.refresh.emit();
    }

    public canChangePassword(): boolean {
        return this.user.username === this.authService.getUsername();
    }

}
