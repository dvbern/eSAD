import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {isNullish} from '@dvb/util-shared';
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
 * config has a value that is different from nullish. The `version` key is stripped at every level of the
 * traversal, not just the top, so a nested object carrying its own version does not falsely count as content.
 */
export function hasSomeNotNullishValueIgnoringVersion(
  defaultUntersuchungsValues: { version: number }
): boolean {
  const stack: unknown[] = [defaultUntersuchungsValues];
  const seen = new WeakSet<object>();

  while (stack.length > 0) {
    const value = stack.pop();

    if (isObject(value)) {
      if (seen.has(value)) {
        continue;
      }
      seen.add(value);
      for (const child of getChildValues(value)) {
        stack.push(child);
      }
    } else if (isMeaningfulLeaf(value)) {
      return true;
    }
  }

  return false;
}

/** Values to descend into: array items, or object values with the `version` key removed. */
function getChildValues(value: object): unknown[] {
  if (Array.isArray(value)) {
    return value;
  }

  return Object.entries(value)
    .filter(([key]) => key !== 'version')
    .map(([, child]) => child);
}

/** A non-container value that counts as content: not nullish, not an empty/whitespace string. */
function isMeaningfulLeaf(value: unknown): boolean {
  if (isNullish(value)) {
    return false;
  }
  if (typeof value === 'string') {
    return value.trim() !== '';
  }

  return true;
}

function isObject(value: unknown): value is object {
  return !isNullish(value) && typeof value === 'object';
}
