import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {isNotNullish} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

export function markAllMultiCheckboxesAsTouched(formArray: FormArray<FormGroup>, destroyRef: DestroyRef) {
  formArray.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(destroyRef))
    .subscribe({
      next: () => {
        if (formArray.touched) {
          formArray.markAllAsTouched();
        }
      },
      error: console.error,
    });
}

export function checkIfArrayContainsCheckedValue<T>(controls: FormGroup<{
  resultat: FormControl<T>;
  isChecked: FormControl<boolean>;
}>[], filterValues: T[]) {
  return controls.some(group => {
    const resultat = group.controls.resultat?.getRawValue();
    const isChecked = group.controls.isChecked?.value;
    return isChecked && filterValues.includes(resultat);
  });
}

/**
 * because version is always set and is never nullish but rather 0 we have to ignore it when checking if the
 * config has a value that is different from nullish
 */
export function hasSomeNotNullishValueIgnoringVersion(defaultUntersuchungsValues: { version: number }): boolean {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const {version, ...defaultProperties} = defaultUntersuchungsValues;

  return Object.values(defaultProperties).some(isNotNullish);
}
