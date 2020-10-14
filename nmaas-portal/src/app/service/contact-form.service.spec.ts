import { TestBed } from '@angular/core/testing';

import { ContactFormService } from './contact-form.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('ContactFormService', () => {
  let service: ContactFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
          HttpClientTestingModule
      ]
    });
    service = TestBed.inject(ContactFormService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve default form from assets', () => {
    service.getForm('default').subscribe(
        data => expect(data).toBeTruthy()
    );
  });

  it('should load default when form is missing', () => {
    service.getForm('missing').subscribe(
        data => expect(data).toBeTruthy()
    );
  });
});
