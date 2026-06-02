import {Directive, ElementRef, HostBinding, HostListener, inject, input, Optional, Self} from '@angular/core';
import {ControlValueAccessor, NgControl} from '@angular/forms';
import {noop} from 'rxjs';
import {ControlType, ValidatedInputWithIdDirective} from '../validated-input-with-id.directive';

/**
 * Checkbox-Direktive für Single-Select-Felder mit nur einer Option:
 * - Im Gegensatz zur normalen `CheckboxDirective` (boolean) schreibt diese Direktive einen
 *   semantischen String-Wert (z.B. `'NICHT_GEPRUEFT'`) ins FormControl, wenn die Checkbox
 *   angehakt ist, und `undefined`, wenn sie abgewählt ist.
 * - Sie ist sowohl `ValidatedInputWithIdDirective` (für `is-invalid`-Class und id) als auch
 *   `ControlValueAccessor` (damit `[formControl]` mit String-Wert funktioniert).
 *
 * Verwendung im Template:
 * ```html
 * <input svuiSingleValueCheckbox
 *        type="checkbox"
 *        [value]="myStringValue"
 *        [formControl]="myFormControl"/>
 * ```
 */
@Directive({
  selector: 'input[svuiSingleValueCheckbox]',
  standalone: true,
  providers: [
    {
      // See comment on ValidatedInputWithIdDirective for reasons
      provide: ValidatedInputWithIdDirective,
      useExisting: SingleValueCheckboxDirective,
    },
    // Achtung: KEIN NG_VALUE_ACCESSOR-Provider hier! Das würde eine zirkuläre Abhängigkeit
    // mit dem injizierten NgControl verursachen (NG0200). Stattdessen wird die Direktive
    // im Konstruktor manuell als ValueAccessor an NgControl gehängt (ngControl.valueAccessor = this).
  ],
})
export class SingleValueCheckboxDirective extends ValidatedInputWithIdDirective implements ControlValueAccessor {
  // type must be specified in caller explicitly, else Angular won't get it
  readonly type = input.required<'checkbox'>();

  /** Der String-Wert, der ins FormControl geschrieben wird, wenn die Checkbox angehakt ist. */
  readonly value = input.required<string>();

  @HostBinding('class.form-check-input') readonly bootstrapClass = true;

  private readonly elementRef = inject<ElementRef<HTMLInputElement>>(ElementRef);

  private onChange: (value: string | undefined) => void = noop;
  private onTouched: () => void = noop;

  // eslint-disable-next-line @angular-eslint/prefer-inject
  constructor(@Optional() @Self() ngControl: NgControl | null) {
    super(ngControl);
    if (ngControl) {
      ngControl.valueAccessor = this;
    }
  }

  get controlType(): ControlType {
    return 'checkbox';
  }

  get isLabelFloatable(): boolean {
    return false;
  }

  @HostListener('change', ['$event'])
  handleChange(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.onChange(checked ? this.value() : undefined);
    this.onTouched();
  }

  @HostListener('blur')
  handleBlur(): void {
    this.onTouched();
  }

  writeValue(value: string | undefined | null): void {
    this.elementRef.nativeElement.checked = value === this.value();
  }

  registerOnChange(fn: (value: string | undefined) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.elementRef.nativeElement.disabled = isDisabled;
  }
}
