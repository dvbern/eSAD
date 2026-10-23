import {DestroyRef, inject} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {NonNullableFormBuilder} from '@angular/forms';
import {
  AnlassConfigJson,
  BenutzerOptionJson,
  ImpfstoffJson,
  Krankheit,
  RequirementSetting,
  UntersuchungImpfungInternSaveJson,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {getChargeOptions, ImpfstoffChargeOption, ImpfstoffeChargenStore, TimetravelStore} from '@dvb/feat-shared';
import {Nullish} from '@dvb/util-shared';
import {UntersuchungImpfungenInternForm} from './untersuchung-impfungen-intern.form';

const impfstoffMitChargen = {
  id: 'impfstoff-1' as BackendEntityId,
  data: {
    name: 'Impfstoff 1',
    krankheiten: [Krankheit.MASERN],
  },
} as ImpfstoffJson;

const impfstoffOhneChargen = {
  id: 'impfstoff-2' as BackendEntityId,
  data: {
    name: 'Impfstoff 2',
    krankheiten: [Krankheit.MUMPS],
  },
} as ImpfstoffJson;

const aktiveChargenNamen: Record<string, string[]> = {
  'impfstoff-1': ['Charge 1', 'Charge 2'],
  'impfstoff-2': [],
};

function makeStore(): ImpfstoffeChargenStore {
  return {
    chargeOptionsForImpfstoffIncluding(
      impfstoffId: BackendEntityId | undefined,
      currentChargeName: string | Nullish,
    ): ImpfstoffChargeOption[] {
      const namen = impfstoffId ? aktiveChargenNamen[impfstoffId] ?? [] : [];

      return getChargeOptions(
        namen.map(name => ({data: {name, aktiv: true}})) as never,
        currentChargeName);
    },
  } as unknown as ImpfstoffeChargenStore;
}

const timetravelStore = {
  currentDate: () => new Date(2026, 0, 1),
} as unknown as TimetravelStore;

const anlassConfig = {
  data: {impfungenListe: RequirementSetting.OPTIONAL},
} as AnlassConfigJson;

const currentUser = {id: 'benutzer-1' as BackendEntityId, fullName: 'Test User'} as BenutzerOptionJson;

function createForm(): UntersuchungImpfungenInternForm {
  return TestBed.runInInjectionContext(() => new UntersuchungImpfungenInternForm({
    fb: TestBed.inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    impfstoffChargenStore: makeStore(),
    timetravelStore,
  }));
}

describe('UntersuchungImpfungenInternForm', () => {
  describe('chargeOptions', () => {
    it('fills the charge options as soon as an impfstoff is selected', () => {
      const form = createForm();
      form.resetForm(undefined, anlassConfig, 'create', currentUser, timetravelStore);

      form.group.controls.impfstoff.setValue(impfstoffMitChargen);

      expect(form.chargeOptions())
        .toEqual([{label: 'Charge 1', inaktiv: false}, {label: 'Charge 2', inaktiv: false}]);
    });

    it('clears the charge options when an impfstoff without active chargen is selected', () => {
      const form = createForm();
      form.resetForm(undefined, anlassConfig, 'create', currentUser, timetravelStore);
      form.group.controls.impfstoff.setValue(impfstoffMitChargen);

      form.group.controls.impfstoff.setValue(impfstoffOhneChargen);

      expect(form.chargeOptions())
        .toEqual([]);
    });

    it('keeps the already saved charge in the options when an existing impfung is loaded', () => {
      const form = createForm();
      const impfung = {
        impfstoff: impfstoffMitChargen,
        charge: 'Charge inaktiv',
        krankheitenImmunitaeten: [],
      } as unknown as UntersuchungImpfungInternSaveJson;

      form.resetForm(impfung, anlassConfig, 'edit', currentUser, timetravelStore);

      expect(form.chargeOptions())
        .toEqual([
          {label: 'Charge 1', inaktiv: false},
          {label: 'Charge 2', inaktiv: false},
          {label: 'Charge inaktiv', inaktiv: true},
        ]);
      expect(form.group.controls.charge.value)
        .toEqual({label: 'Charge inaktiv'});
    });
  });
});
