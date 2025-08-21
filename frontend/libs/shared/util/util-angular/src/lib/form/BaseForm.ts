import {DestroyRef, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AbstractControl, FormArray, FormControl, FormGroup, ɵGetProperty} from '@angular/forms';
import {AppValidationErrorJson, ViolationJson} from '@dvb/data-access-backend';
import {className, isNotNullish, Leaves, Paths} from '@dvb/util-shared';
import {debounceTime, distinctUntilChanged, Observable} from 'rxjs';
import {DirtyMarker} from '../dirty-guard';
import {isAppValidationErorrJson, isFieldViolation} from '../error-handling';
import {DetailMode} from '../store';
import {AppValidatorKeys} from './AppValidators';
import {whenFormValid} from './when-form-valid';

export abstract class BaseForm<Form extends FormGroup, DTO extends object> implements DirtyMarker {
  group: Form;
  mode: DetailMode = DetailMode.EDIT;

  protected constructor(
    builder: () => Form,
    protected readonly destroyRef = inject(DestroyRef),
  ) {
    this.group = builder();
  }

  reset(): void;
  reset(value: DTO | undefined): void;
  reset(value: DTO | undefined, mode: DetailMode): void;
  reset(value: DTO | undefined, mode: DetailMode, errors: AppValidationErrorJson | undefined): void;
  reset(value?: DTO | undefined, mode?: DetailMode, errors?: AppValidationErrorJson | undefined): void {
    this.group.reset(value);
    this.mergeFormViolations(errors);
    if (errors?.violations?.length ?? 0 > 0) {
      // FIXME: starter: decide if this is a thing for your project
      this.group.markAsDirty();
    }
    if (isNotNullish(mode)) {
      this.mode = mode;
    }
  }

  abstract valueOnValid(): DTO;

  /**
   * @example
   * async function onSubmit() {
   *   if (await form.validate()) {
   *     this.store.submit(form.valueOnValid());
   *   }
   * }
   */
  async validate(): Promise<boolean> {
    return await whenFormValid(this.group, this.destroyRef);
  }

  /**
   * Usage: <pre>{{ debugFormStatus(form.group) | json }}</pre>
   */
  debugFormStatus(ctl: AbstractControl = this.group): Record<string, unknown> | string {
    // noinspection IfStatementWithTooManyBranchesJS
    if (ctl instanceof FormGroup || ctl instanceof FormArray) {
      const next: Record<string, unknown> = {};
      Object.entries(ctl.controls).forEach(([childName, c]) => {
        next[childName] = this.debugFormStatus(c);
      });
      return next;
    } else if (ctl instanceof FormControl) {
      return ctl.status;
    } else {
      throw Error(`Not supported: ${typeof ctl} = ${className(ctl)}`);
    }
  }

  isDirty(): boolean {
    return this.group.dirty;
  }

  protected mergeFormViolations(validationErrors: AppValidationErrorJson | undefined): void;
  protected mergeFormViolations(violations: ViolationJson[] | undefined): void;
  protected mergeFormViolations(
    validationErrorOfViolations: AppValidationErrorJson | ViolationJson[] | undefined,
  ): void {
    // this.clearBackendErrors();
    const violations: ViolationJson[] | undefined = isAppValidationErorrJson(validationErrorOfViolations)
      ? validationErrorOfViolations.violations
      : validationErrorOfViolations;

    violations
      ?.filter(violation => isFieldViolation(violation))
      ?.forEach(violation => this.mergeViolationIntoFieldErrors(violation));
  }

  private mergeViolationIntoFieldErrors(violation: ViolationJson): void {
    const control = this.group.get(violation.path);
    if (!control) {
      throw Error(`could not find control for path: ${violation.path}`);
    }

    const oldErrors = control.errors || {};
    const newErrors = {
      ...oldErrors,
      [AppValidatorKeys.backend]: violation.message,
    };

    control.setErrors(newErrors);
  }

  controlAt<Path extends Leaves<DTO>>(path: Path): FormControl<ɵGetProperty<DTO, Path>> {
    const ctl = this.group.get(path);
    if (!(ctl instanceof FormControl)) {
      throw Error(`This is not a FormControl: ${path}`);
    }
    return ctl;
  }

  arrayAt<Path extends Paths<DTO>>(path: Path): FormArray<ɵGetProperty<DTO, Path>> {
    const ctl = this.group.get(path);
    if (!(ctl instanceof FormArray)) {
      throw Error(`This is not a FormArray: ${path}`);
    }
    return ctl;
  }

  groupAt<Path extends Paths<DTO>>(path: Path): FormGroup<ɵGetProperty<DTO, Path>> {
    const ctl = this.group.get(path);
    if (!(ctl instanceof FormGroup)) {
      throw Error(`This is not a FormGroup: ${path}`);
    }
    return ctl;
  }

  setDisabled(disabled: boolean): void {
    if (disabled) {
      this.group.disable();
    } else {
      this.group.enable();
    }
  }
}

const DEBOUNCE_TIME_TYPEAHEAD_MS = 250;

export function onTextInput<T>(control: FormControl<T>, destroyRef: DestroyRef): Observable<T> {
  return control.valueChanges
    .pipe(
      debounceTime(DEBOUNCE_TIME_TYPEAHEAD_MS),
      distinctUntilChanged(),
      takeUntilDestroyed(destroyRef));
}
