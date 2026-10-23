import {signal} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {FormControl, FormGroup} from '@angular/forms';
import {globalConstants} from '@dvb/feat-shared';
import {Nullish} from '@dvb/util-shared';
import {patchState, signalStore, withState} from '@ngrx/signals';
import {ResettableForm, setupApplyDefaultValuesEffect, withDefaultValues} from './default-values.util';
import type {Mock} from 'vitest';

interface TestDto {
  version?: number;
  name?: string | null;
}

interface TestEntity {
  data?: {
    version?: number;
  };
}

class TestForm implements ResettableForm<TestDto> {
  group = new FormGroup({
    name: new FormControl<string | null>(null),
  });

  resetForm = vi.fn((value?: TestDto | Nullish) => {
    if (value) {
      this.group.patchValue({name: value.name});
    }
  });

  setDisabled = vi.fn((disabled: boolean) => {
    if (disabled) {
      this.group.disable();
    } else {
      this.group.enable();
    }
  });
}

type TestState = {
  entity: TestEntity | undefined;
};

describe('withDefaultValues', () => {
  const TestStore = signalStore(
    {protectedState: false},
    withState<TestState>({ entity: undefined }),
    withDefaultValues<TestDto, TestEntity, TestState>({
      getCurrentEntity: (store) => store.entity(),
    }),
  );

  let store: InstanceType<typeof TestStore>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TestStore],
    });
    store = TestBed.inject(TestStore);
  });

  it('should initialize with undefined defaultInitial and defaultToApply', () => {
    expect(store.defaultInitial()).toBeUndefined();
    expect(store.defaultToApply()).toBeUndefined();
  });

  it('should do nothing on applyDefault when defaultInitial is undefined', () => {
    store.applyDefault();
    expect(store.defaultToApply()).toBeUndefined();
  });

  it('should report the defaults as loaded only once they have been fetched', () => {
    expect(store.defaultsLoaded()).toBe(false);

    // an Anlass without configured defaults still gets an object, just with empty values
    patchState(store, { defaultInitial: { version: 0, name: null } });

    expect(store.defaultsLoaded()).toBe(true);
  });

  it('should apply defaults with version from entity.data.version', () => {
    patchState(store, {
      defaultInitial: { version: 0, name: 'Default Name' },
      entity: { data: { version: 5 } },
    });

    store.applyDefault();

    expect(store.defaultToApply()).toEqual({
      version: 5,
      name: 'Default Name',
    });
  });

  it('should fallback to globalConstants.entityPrePersistVersion when entity has no version', () => {
    patchState(store, {
      defaultInitial: { version: 0, name: 'Default Name' },
      entity: undefined,
    });

    store.applyDefault();

    expect(store.defaultToApply()).toEqual({
      version: globalConstants.entityPrePersistVersion,
      name: 'Default Name',
    });
  });

  it('should reset defaultToApply to undefined on resetApplyDefault', () => {
    patchState(store, {
      defaultInitial: { version: 0, name: 'Default Name' },
    });
    store.applyDefault();
    expect(store.defaultToApply()).toBeDefined();

    store.resetApplyDefault();
    expect(store.defaultToApply()).toBeUndefined();
  });

  it('should apply defaults with version directly from entity.version', () => {
    interface RootVersionEntity {
      version?: number;
    }
    const RootVersionStore = signalStore(
      {protectedState: false},
      withState<{ entity: RootVersionEntity | undefined }>({ entity: undefined }),
      withDefaultValues<TestDto, RootVersionEntity, { entity: RootVersionEntity | undefined }>({
        getCurrentEntity: (s) => s.entity(),
      }),
    );
    const rootStore = new RootVersionStore();
    patchState(rootStore, {
      defaultInitial: { version: 0, name: 'Default Name' },
      entity: { version: 7 },
    });

    rootStore.applyDefault();

    expect(rootStore.defaultToApply()).toEqual({
      version: 7,
      name: 'Default Name',
    });
  });

  it('should fallback to globalConstants.entityPrePersistVersion when no config is passed', () => {
    const NoConfigStore = signalStore(
      {protectedState: false},
      withState({ dummy: '' }),
      withDefaultValues<TestDto, Nullish>(),
    );
    const noConfigStore = new NoConfigStore();
    patchState(noConfigStore, {
      defaultInitial: { version: 0, name: 'No Config' },
    });

    noConfigStore.applyDefault();

    expect(noConfigStore.defaultToApply()).toEqual({
      version: globalConstants.entityPrePersistVersion,
      name: 'No Config',
    });
  });
});

describe('setupApplyDefaultValuesEffect', () => {
  let form: TestForm;
  let resetApplyDefault: Mock;

  beforeEach(() => {
    form = new TestForm();
    resetApplyDefault = vi.fn();
    vi.spyOn(form.group, 'markAsDirty');
  });

  it('should not do anything when defaultValuesToApply is nullish', () => {
    const defaultValuesSignal = signal<TestDto | Nullish>(null);
    const canEditSignal = signal(true);

    TestBed.runInInjectionContext(() => {
      setupApplyDefaultValuesEffect({
        defaultValuesToApply: defaultValuesSignal,
        form,
        resetApplyDefault,
        canEdit: canEditSignal,
      });
    });

    TestBed.flushEffects();

    expect(form.resetForm).not.toHaveBeenCalled();
    expect(form.group.markAsDirty).not.toHaveBeenCalled();
    expect(resetApplyDefault).not.toHaveBeenCalled();
    expect(form.setDisabled).not.toHaveBeenCalled();
  });

  it('should reset apply default and update disabled state without resetting form when payload contains only version', () => {
    const defaultValuesSignal = signal<TestDto | Nullish>({version: 0});
    const canEditSignal = signal(true);

    TestBed.runInInjectionContext(() => {
      setupApplyDefaultValuesEffect({
        defaultValuesToApply: defaultValuesSignal,
        form,
        resetApplyDefault,
        canEdit: canEditSignal,
      });
    });

    TestBed.flushEffects();

    expect(form.resetForm).not.toHaveBeenCalled();
    expect(form.group.markAsDirty).not.toHaveBeenCalled();
    expect(resetApplyDefault).toHaveBeenCalledTimes(1);
    expect(form.setDisabled).toHaveBeenCalledWith(false);
  });

  it('should apply default values, mark dirty, call onAfterApply, reset apply flag, and set disabled state when values are present', () => {
    const testDto: TestDto = {version: 0, name: 'Dr. Test'};
    const defaultValuesSignal = signal<TestDto | Nullish>(testDto);
    const canEditSignal = signal(true);
    const onAfterApply = vi.fn();

    TestBed.runInInjectionContext(() => {
      setupApplyDefaultValuesEffect({
        defaultValuesToApply: defaultValuesSignal,
        form,
        resetApplyDefault,
        canEdit: canEditSignal,
        onAfterApply,
      });
    });

    TestBed.flushEffects();

    expect(form.resetForm).toHaveBeenCalledWith(testDto);
    expect(form.group.markAsDirty).toHaveBeenCalledTimes(1);
    expect(onAfterApply).toHaveBeenCalledWith(form, testDto);
    expect(resetApplyDefault).toHaveBeenCalledTimes(1);
    expect(form.setDisabled).toHaveBeenCalledWith(false);
  });

  it('should disable the form if canEdit is false', () => {
    const testDto: TestDto = {version: 1, name: 'Dr. Test'};
    const defaultValuesSignal = signal<TestDto | Nullish>(testDto);
    const canEditSignal = signal(false);

    TestBed.runInInjectionContext(() => {
      setupApplyDefaultValuesEffect({
        defaultValuesToApply: defaultValuesSignal,
        form,
        resetApplyDefault,
        canEdit: canEditSignal,
      });
    });

    TestBed.flushEffects();

    expect(form.setDisabled).toHaveBeenCalledWith(true);
  });

  it('should work when passing getter functions instead of signals', () => {
    const testDto: TestDto = {version: 0, name: 'Getter Test'};
    const defaultValues: TestDto | Nullish = testDto;

    TestBed.runInInjectionContext(() => {
      setupApplyDefaultValuesEffect({
        defaultValuesToApply: () => defaultValues,
        form,
        resetApplyDefault,
        canEdit: () => true,
      });
    });

    TestBed.flushEffects();

    expect(form.resetForm).toHaveBeenCalledWith(testDto);
    expect(resetApplyDefault).toHaveBeenCalledTimes(1);
    expect(form.setDisabled).toHaveBeenCalledWith(false);
  });

  it('should apply defaults for versionless DTO forms', () => {
    interface VersionlessDto {
      name: string;
    }
    class VersionlessForm implements ResettableForm<VersionlessDto> {
      group = new FormGroup({
        name: new FormControl<string | null>(null),
      });
      resetForm = vi.fn();
      setDisabled = vi.fn();
    }

    const versionlessForm = new VersionlessForm();
    const versionlessDto: VersionlessDto = { name: 'Versionless Form Test' };
    const defaultValuesSignal = signal<VersionlessDto | Nullish>(versionlessDto);

    TestBed.runInInjectionContext(() => {
      setupApplyDefaultValuesEffect<VersionlessDto, VersionlessForm>({
        defaultValuesToApply: defaultValuesSignal,
        form: versionlessForm,
        resetApplyDefault,
        canEdit: () => true,
      });
    });

    TestBed.flushEffects();

    expect(versionlessForm.resetForm).toHaveBeenCalledWith(versionlessDto);
    expect(resetApplyDefault).toHaveBeenCalledTimes(1);
    expect(versionlessForm.setDisabled).toHaveBeenCalledWith(false);
  });
});
