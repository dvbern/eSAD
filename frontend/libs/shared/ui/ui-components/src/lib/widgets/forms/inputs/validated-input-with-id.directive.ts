import {
  AfterViewInit,
  ChangeDetectorRef,
  Directive,
  HostBinding,
  inject,
  Input,
  model,
  Optional,
  Self,
} from '@angular/core';
import { NgControl } from '@angular/forms';
import { Nullish } from '@dvb/util-shared';
import { classBootstrapInvalid, classInput } from '../../styles-helper/styles-helper';

export type ControlType = 'checkbox' | 'radios' | 'select' | 'ngSelect' | 'textInput' | 'textarea' | 'typeahead';

// For some reason, subclasses of ValidatedInputWithIdDirective are not recognized as providers for
// ValidatedInputWithIdDirective <strong>in unit tests only!!!</strong>
// Because of that, every subclass must provide itself as a provider for ValidatedInputWithIdDirective.
@Directive({ standalone: true })
export abstract class ValidatedInputWithIdDirective implements AfterViewInit {
  @HostBinding('class.' + classInput) private readonly classInput = true;

  private static idGenerator = 0;
  #id: string | Nullish;

  readonly cdRef = inject(ChangeDetectorRef);

  @Input()
  @HostBinding('id')
  set id(id: string | Nullish) {
    this.#id = id;
  }

  get id(): string {
    // lazy init
    if (!this.#id) {
      this.#id = 'auto-id-' + ValidatedInputWithIdDirective.idGenerator++;
    }
    return this.#id;
  }

  @HostBinding('class.' + classBootstrapInvalid)
  get isInvalid(): boolean {
    if (!this.ngControl) {
      return false;
    }

    const invalid = this.ngControl.invalid ?? false;
    const touched = this.ngControl.touched ?? false;

    /*
    Check for invalid and touched: the invalid is shown when the user blurs the field.
    (for the reference: dirty is when the user is changing the value, even before blur, but we want to wait until blur)
    https://angular.dev/guide/forms/form-validation#validating-input-in-template-driven-forms
     */
    const result = invalid && touched;

    return result;
  }

  // FIXME(zusa) sollte abstract & optional sein
  readonly working = model<boolean>(false);

  abstract get controlType(): ControlType;

  abstract get isLabelFloatable(): boolean;

  constructor(@Optional() @Self() public ngControl: NgControl) {}

  ngAfterViewInit(): void {
    // change detection when form control was touched programmatically
    if (this.ngControl?.statusChanges) {
      this.ngControl.statusChanges.subscribe({
        next: () => this.cdRef.markForCheck(),
      });
    }
  }
}
