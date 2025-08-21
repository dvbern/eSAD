import { Directive } from '@angular/core';
import { Nullish } from '@dvb/util-shared';

@Directive({ standalone: true })
export class WithIdDirective {
  private static idGenerator = 0;
  #id: string | Nullish;

  get id(): string {
    // lazy init
    if (!this.#id) {
      this.#id = 'auto-id-' + WithIdDirective.idGenerator++;
    }
    return this.#id;
  }
}
