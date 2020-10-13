import {User} from '../../../model/user';
import {UserService} from '../../../service/user.service';
import {BaseComponent} from '../../../shared/common/basecomponent/base.component';
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../../auth/auth.service';
import {ComponentMode} from '../../../shared';

@Component({
    selector: 'app-userdetails',
    templateUrl: './userdetails.component.html',
    styleUrls: ['./userdetails.component.css']
})
export class UserDetailsComponent extends BaseComponent implements OnInit {

    private userId: number;
    public user: User;
    public errorMessage: string;
    public userDetailsMode: ComponentMode = ComponentMode.VIEW;


    constructor(private userService: UserService, private router: Router,
                private route: ActivatedRoute, public authService: AuthService) {
        super();
    }

    ngOnInit() {
        this.route.params.subscribe(params => {
            if (params['id'] !== undefined) {
                this.userId = +params['id'];
                this.userService.getOne(this.userId).subscribe(
                    (user) => this.user = user,
                    err => {
                        console.error(err);
                        if (err.statusCode && (err.statusCode === 404 || err.statusCode === 401 || err.statusCode === 403)) {
                            this.router.navigateByUrl('/notfound');
                        }
                    });
            }
        });
        this.mode = this.getMode(this.route);
    }

    public onRefresh() {
        this.userService.getOne(this.userId).subscribe((user) => {
            this.user = user;
            this.onModeChange();
            this.errorMessage = undefined;
        });
    }

    public onModeChange() {
        this.userDetailsMode = (this.userDetailsMode === ComponentMode.VIEW ? ComponentMode.EDIT : ComponentMode.VIEW);
    }

    public onSave($event) {
        const user: User = $event;

        if (!!user && user.id) {
            this.updateUser(user.id, user)
        }
    }

    public updateUser(userId: number, user: User): void {
        this.userService.updateUser(userId, user).subscribe(
            result => {
                this.userDetailsMode = ComponentMode.VIEW;
                this.errorMessage = undefined;
            },
            error => {
                this.userDetailsMode = ComponentMode.EDIT;
                this.errorMessage = error.message;
            }
        )
    }

    public remove(userId: number) {
        this.userService.deleteOne(userId).subscribe(() => this.router.navigate(['/users/']));
    }
}
