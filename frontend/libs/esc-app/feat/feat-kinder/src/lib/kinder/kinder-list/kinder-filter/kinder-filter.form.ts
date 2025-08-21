import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {
  AnlassArt,
  AnlassStatus,
  AnlassTyp,
  EinladungStatus,
  Krankheit,
  LabelJson,
  NotizStatus,
} from '@dvb/data-access-backend';
import {BackendEntityId, ISODateString} from '@dvb/data-access-types';
import {BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, isFilledIn} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

export interface ErweiterteFilterJson {
  anlassPendent: boolean;

  zeitraumVon: ISODateString | undefined;
  zeitraumBis: ISODateString | undefined;
  schuljahr: number | undefined;
  verantwortlicherId: BackendEntityId | undefined;
  labels: LabelJson[]
  status: NotizStatus | undefined;

  inaktiv: boolean;
  mpaId: BackendEntityId | undefined;
  arztId: BackendEntityId | undefined;
  immunitaetVariezellen: boolean | undefined;
  immunitaetMasern: boolean | undefined;

  krankheit: Krankheit | undefined;
  impfdatumVon: ISODateString | undefined;
  impfdatumBis: ISODateString | undefined;
  impfungCharge: BackendEntityId | undefined;

  anlassZeitraumVon: ISODateString | undefined;
  anlassZeitraumBis: ISODateString | undefined;
  anlassSchuljahr: number | undefined;
  anlassStatus: AnlassStatus | undefined;
  anlassArt: AnlassArt | undefined;
  anlassTyp: AnlassTyp | undefined;
  anlassMpaId: BackendEntityId | undefined;
  anlassArztId: BackendEntityId | undefined;
  anlassWeiterePerson: BackendEntityId | undefined;
  anlassTeilnahmeStatus: EinladungStatus | undefined;
  anlassImpfungMassnahmeVormerkungFolgetermin: boolean | undefined;
  dossierNachkontrolle: boolean | undefined,
  anlassNotAssigned: boolean | undefined;
}

export const DEFAULT_AKTIVE_ONLY_FILTER: ErweiterteFilterJson = {
  anlassPendent: false,

  zeitraumVon: undefined,
  zeitraumBis: undefined,
  schuljahr: undefined,
  verantwortlicherId: undefined,
  labels: [],
  status: undefined,

  inaktiv: false,
  mpaId: undefined,
  arztId: undefined,
  immunitaetVariezellen: undefined,
  immunitaetMasern: undefined,

  krankheit: undefined,
  impfdatumVon: undefined,
  impfdatumBis: undefined,
  impfungCharge: undefined,

  anlassZeitraumVon: undefined,
  anlassZeitraumBis: undefined,
  anlassSchuljahr: undefined,
  anlassStatus: undefined,
  anlassArt: undefined,
  anlassTyp: undefined,
  anlassMpaId: undefined,
  anlassArztId: undefined,
  anlassWeiterePerson: undefined,
  anlassTeilnahmeStatus: undefined,
  anlassImpfungMassnahmeVormerkungFolgetermin: undefined,
  dossierNachkontrolle: undefined,
  anlassNotAssigned: undefined,
};

function makeForm(fb: NonNullableFormBuilder, destroy: DestroyRef) {
  const group = fb.group({
    anlassPendent: fb.control<boolean>(false),

    zeitraumVon: fb.control<ISODateString | ''>(''),
    zeitraumBis: fb.control<ISODateString | ''>(''),
    schuljahr: fb.control<number | undefined>(undefined),
    verantwortlicherId: fb.control<BackendEntityId | undefined>(undefined),
    labels: fb.control<LabelJson[]>([]),
    status: fb.control<NotizStatus | undefined>(undefined),

    inaktiv: fb.control<boolean>(false),
    mpaId: fb.control<BackendEntityId | undefined>(undefined),
    arztId: fb.control<BackendEntityId | undefined>(undefined),
    immunitaetVariezellen: fb.control<boolean | undefined>(undefined),
    immunitaetMasern: fb.control<boolean | undefined>(undefined),

    krankheit: fb.control<Krankheit | undefined>(undefined),
    impfdatumVon: fb.control<ISODateString | ''>(''),
    impfdatumBis: fb.control<ISODateString | ''>(''),
    impfungCharge: fb.control<BackendEntityId | undefined>(undefined),

    anlassZeitraumVon: fb.control<ISODateString | ''>(''),
    anlassZeitraumBis: fb.control<ISODateString | ''>(''),
    anlassSchuljahr: fb.control<number | undefined>(undefined),
    anlassStatus: fb.control<AnlassStatus | undefined>(undefined),
    anlassArt: fb.control<AnlassArt | undefined>(undefined),
    anlassTyp: fb.control<AnlassTyp | undefined>(undefined),
    anlassMpaId: fb.control<BackendEntityId | undefined>(undefined),
    anlassArztId: fb.control<BackendEntityId | undefined>(undefined),
    anlassWeiterePerson: fb.control<BackendEntityId | undefined>(undefined),
    anlassTeilnahmeStatus: fb.control<EinladungStatus | undefined>(undefined),
    anlassImpfungMassnahmeVormerkungFolgetermin: fb.control<boolean | undefined>(undefined),
    dossierNachkontrolle: fb.control<boolean | undefined>(undefined),
    anlassNotAssigned: fb.control<boolean | undefined>(undefined),
  });

  group.controls.schuljahr.valueChanges.pipe(takeUntilDestroyed(destroy), distinctUntilChanged()).subscribe(
    value => {
      if (value) {
        group.controls.zeitraumVon.reset();
        group.controls.zeitraumBis.reset();
      }
    },
  );

  group.controls.zeitraumVon.valueChanges.pipe(takeUntilDestroyed(destroy), distinctUntilChanged()).subscribe(
    value => {
      if (value) {
        group.controls.schuljahr.reset();
      }
    },
  );

  group.controls.zeitraumBis.valueChanges.pipe(takeUntilDestroyed(destroy), distinctUntilChanged()).subscribe(
    value => {
      if (value) {
        group.controls.schuljahr.reset();
      }
    },
  );

  group.controls.anlassPendent.valueChanges.pipe(takeUntilDestroyed(destroy), distinctUntilChanged()).subscribe(
    value => {
      // Reset all fields except anlassPendent
      group.reset({
        anlassPendent: value
      });
    },
  );

  return group;
}

export class KinderFilterForm extends BaseForm<
  ReturnType<typeof makeForm>,
  ErweiterteFilterJson
> {
  constructor(fb: NonNullableFormBuilder, destroy: DestroyRef) {
    super(() => makeForm(fb, destroy));
  }

  isStandardFilterSelected(): boolean {
    return this.group.get('anlassPendent')?.value !== true;
  }

  valueOnValid(): ErweiterteFilterJson {
    const value = this.group.getRawValue();
    return ensurePropsFlat<ErweiterteFilterJson>({
      anlassPendent: value.anlassPendent,
      labels: value.labels,
      schuljahr: value.schuljahr,
      status: value.status,
      verantwortlicherId: value.verantwortlicherId,
      zeitraumBis: isFilledIn(value.zeitraumBis) ? value.zeitraumBis : undefined,
      zeitraumVon: isFilledIn(value.zeitraumVon) ? value.zeitraumVon : undefined,
      inaktiv: value.inaktiv,
      mpaId: value.mpaId,
      arztId: value.arztId,
      dossierNachkontrolle: value.dossierNachkontrolle,
      immunitaetVariezellen: value.immunitaetVariezellen,
      immunitaetMasern: value.immunitaetMasern,
      krankheit: value.krankheit,
      impfdatumVon: isFilledIn(value.impfdatumVon) ? value.impfdatumVon : undefined,
      impfdatumBis: isFilledIn(value.impfdatumBis) ? value.impfdatumBis : undefined,
      impfungCharge: value.impfungCharge,
      anlassZeitraumVon: isFilledIn(value.anlassZeitraumVon) ? value.anlassZeitraumVon : undefined,
      anlassZeitraumBis: isFilledIn(value.anlassZeitraumBis) ? value.anlassZeitraumBis : undefined,
      anlassSchuljahr: value.anlassSchuljahr,
      anlassStatus: value.anlassStatus,
      anlassArt: value.anlassArt,
      anlassTyp: value.anlassTyp,
      anlassMpaId: value.anlassMpaId,
      anlassArztId: value.anlassArztId,
      anlassWeiterePerson: value.anlassWeiterePerson,
      anlassTeilnahmeStatus: value.anlassTeilnahmeStatus,
      anlassImpfungMassnahmeVormerkungFolgetermin: value.anlassImpfungMassnahmeVormerkungFolgetermin,
      anlassNotAssigned: value.anlassNotAssigned,
    })
  }
}
