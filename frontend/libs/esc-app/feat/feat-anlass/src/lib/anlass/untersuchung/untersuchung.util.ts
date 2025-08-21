import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {distinctUntilChanged} from 'rxjs';

export function markAllMultiCheckboxesAsTouched(formArray: FormArray<FormGroup>, destroyRef: DestroyRef) {
  formArray.valueChanges.pipe(distinctUntilChanged(),  takeUntilDestroyed(destroyRef))
    .subscribe({
      next: () => {
        if(formArray.touched) {
          formArray.markAllAsTouched();
        }
      },
      error: console.error,
    });
}

export function checkIfArrayContainsCheckedValue<T>(controls: FormGroup<{ resultat: FormControl<T>; isChecked: FormControl<boolean>; }>[], filterValues: T[]) {
  return controls.some(group => {
    const resultat = group.controls.resultat?.getRawValue();
    const isChecked = group.controls.isChecked?.value;
    return isChecked && filterValues.includes(resultat);
  });
}
