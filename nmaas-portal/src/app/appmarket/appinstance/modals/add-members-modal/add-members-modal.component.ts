import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from '../../../../shared/modal';
import {AppInstance, User} from '../../../../model';
import {AppInstanceService, UserService} from '../../../../service';
import {SelectItem} from 'primeng/api';

@Component({
    selector: 'app-add-members-modal',
    templateUrl: './add-members-modal.component.html',
    styleUrls: ['./add-members-modal.component.css']
})
export class AddMembersModalComponent implements OnInit {

    @ViewChild(ModalComponent, {static: true})
    public readonly modal: ModalComponent;

    @Input()
    public appInstance: AppInstance = undefined;

    public users: User[] = [];

    public members: User[] = [];

    constructor(public userService: UserService, public appInstanceService: AppInstanceService) {
    }

    ngOnInit(): void {
        this.userService.getAll(this.appInstance.domainId).subscribe(
            data => {
                this.users = data.filter(u => u.username !== this.appInstance.owner.username)
                // retrieve members identifiers
                const memberIds = this.members.map(m => m.id);
                // rewrite members to get full users data instead of only firstname and lastname
                this.members = this.users.filter(u => memberIds.includes(u.id));
            },
            error => console.error(error),
            () => console.log('Domain Users list download completed')
        )
        this.members = this.appInstance.members;
    }

    public show() {
        this.modal.show();
    }

    public hide() {
        this.modal.hide();
    }

    public submit() {
        this.appInstanceService.updateAppInstanceMembers(this.appInstance.id, this.members).subscribe(
            () => console.log('Updated members'),
            error => console.error('Error updating members', error),
            () => this.hide()
        )
    }

}
