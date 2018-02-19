import {Domain} from '../../../model/domain';
import {DomainService} from '../../../service/domain.service';
import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-domains-list',
  templateUrl: './domainslist.component.html',
  styleUrls: ['./domainslist.component.css']
})
export class DomainsListComponent implements OnInit {

  private domains: Domain[];

  constructor(protected domainService: DomainService) {}

  ngOnInit() {
    this.domainService.getAll().subscribe(
      (domains: Domain[]) => this.domains = domains
    );
  }

  public remove(domainId: number): void {
    this.domainService.remove(domainId);
  }

}
