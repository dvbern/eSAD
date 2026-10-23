import {computed, effect, EffectRef, Injector, Signal} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {globalConstants} from '@dvb/feat-shared';
import {isNotNullish, Nullish} from '@dvb/util-shared';
import {patchState, signalStoreFeature, withComputed, withMethods, withState} from '@ngrx/signals';
import {hasSomeNotNullishValueIgnoringVersion} from './untersuchung.util';

export interface DefaultValuesState<TDto> {
  defaultInitial: TDto | undefined;
  defaultToApply: TDto | undefined;
}

type StoreSignals<T> = T extends Record<string, unknown>
  ? { [K in keyof T]: Signal<T[K]> }
  : T;

type VersionedEntity = {
  version?: unknown;
  data?: { version?: unknown };
};

function resolveEntityVersion(entity: VersionedEntity | Nullish): number {
  const version = entity?.version ?? entity?.data?.version;
  return typeof version === 'number' ? version : globalConstants.entityPrePersistVersion;
}
export function withDefaultValues<
  TDto extends object,
  TEntity extends VersionedEntity | Nullish,
  TState = unknown,
>(config?: {
  getCurrentEntity?: (store: StoreSignals<TState>) => TEntity | Nullish;
}) {
  return signalStoreFeature(
    withState<DefaultValuesState<TDto>>({
      defaultInitial: undefined,
      defaultToApply: undefined,
    }),
    withComputed((store) => ({
      /**
       * Whether the defaults have been fetched. Until then applyDefault() has nothing to apply and
       * returns without a trace, so the "Standardwerte" button has to stay disabled rather than
       * swallow the click. A tab without configured defaults counts as loaded: there is nothing to wait for.
       */
      defaultsLoaded: computed(() => store.defaultInitial() !== undefined),
    })),
    withMethods((store) => ({
      applyDefault(): void {
        const initial = store.defaultInitial();
        if (!initial) {
          return;
        }

        const currentEntity = config?.getCurrentEntity?.(store as StoreSignals<TState>);
        const corrected: TDto = {
          ...initial,
          version: resolveEntityVersion(currentEntity),
        };

        patchState(store, {defaultToApply: corrected});
      },
      resetApplyDefault(): void {
        patchState(store, {defaultToApply: undefined});
      },
    }))
  );
}

export interface ResettableForm<TDto> {
  group: FormGroup;
  resetForm(value?: TDto | Nullish): void;
  setDisabled(disabled: boolean): void;
}

export interface ApplyDefaultValuesConfig<
  TDto,
  TForm extends ResettableForm<TDto> = ResettableForm<TDto>,
> {
  /** Signal or getter returning the default values to apply from the store */
  defaultValuesToApply: Signal<TDto | Nullish> | (() => TDto | Nullish);

  /** The form instance with resetForm, group, and setDisabled */
  form: TForm;

  /** Action to clear/reset the default values state in the store */
  resetApplyDefault: () => void;

  /** Signal or getter returning whether editing is allowed */
  canEdit: Signal<boolean> | (() => boolean);

  /** Optional callback executed after resetForm and markAsDirty */
  onAfterApply?: (form: TForm, defaultValues: TDto) => void;

  /** Optional custom injector when called outside constructor */
  injector?: Injector;
}

/**
 * Registers an effect that applies default values to a form when triggered by a store signal.
 */
export function setupApplyDefaultValuesEffect<
  TDto,
  TForm extends ResettableForm<TDto>,
>(config: ApplyDefaultValuesConfig<TDto, TForm>): EffectRef {
  return effect(
    () => {
      const defaultValues = config.defaultValuesToApply();
      if (isNotNullish(defaultValues)) {
        const hasAnyValue = hasSomeNotNullishValueIgnoringVersion(defaultValues);
        if (hasAnyValue) {
          config.form.resetForm(defaultValues);
          config.form.group.markAsDirty();
          config.onAfterApply?.(config.form, defaultValues);
        }
        config.resetApplyDefault();

        const canEdit = typeof config.canEdit === 'function' ? config.canEdit() : config.canEdit;
        config.form.setDisabled(!canEdit);
      }
    },
    { injector: config.injector }
  );
}
