import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable()
export class UserDataService {

  private domainIdSelectionSource = new BehaviorSubject<number>(0);
  public selectedDomainId = this.domainIdSelectionSource.asObservable();

  constructor() {}

  public selectDomainId(domainId: number): void {
    this.domainIdSelectionSource.next(domainId);
  }

}
