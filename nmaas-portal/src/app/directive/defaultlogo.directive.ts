import {Directive, Input, Output, EventEmitter, SimpleChange, ElementRef, NgZone} from '@angular/core';

@Directive({
  selector: 'img[defaultLogo]',
  host: {
    '(error)': 'updateUrl()',
    '[src]': 'src'
  }
})
export class DefaultLogo {
  @Input() src: string;
  @Input() defaultLogo: string;


  constructor(private el: ElementRef, private ngZone: NgZone) {
  }

  updateUrl() {
    this.src = this.defaultLogo;
  }
}
