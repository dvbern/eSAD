import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, model, OnInit} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {
  AnlassArt,
  AnlassStatus,
  AnlassTyp,
  EinladungStatus,
  Krankheit,
  LabelJson,
  NotizStatus,
} from '@dvb/data-access-backend';
import {BackendEntityId, isISODateString, ISODateString, isValidBackendEntityId} from '@dvb/data-access-types';
import {
  AnlassConfigOptionsStore,
  BenutzerOptionsStore,
  BenutzerStore,
  ImpfstoffeChargenStore,
  LabelsStore,
  ModalDialogComponent, TenantSettingsStore,
  TimetravelStore,
  TranslatedStringPipe,
} from '@dvb/feat-shared';
import {
  CheckboxDirective,
  FormControlComponent,
  FormLayoutComponent,
  NgSelectDirective,
  RadioDirective,
  SaveCancelButtonsComponent,
  SingleCheckboxWrapperComponent,
  SingleRadioWrapperComponent,
  TextInputDirective,
  TitleComponent,
} from '@dvb/ui-components';
import {DetailMode, FilterSessionStorage} from '@dvb/util-angular';
import {
  compareFn,
  ensurePropsFlat,
  fromISODateString,
  isNotNullish,
  isNullish,
  isValidEnumValue,
} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {ErweiterteFilterJson, KinderFilterForm} from './kinder-filter.form';

@Component({
  selector: 'feat-kinder-filter',
  imports: [
    ModalDialogComponent,
    TranslocoPipe,
    SaveCancelButtonsComponent,
    FormControlComponent,
    FormLayoutComponent,
    FormsModule,
    TextInputDirective,
    ReactiveFormsModule,
    TitleComponent,
    DatePipe,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TranslatedStringPipe,
    CheckboxDirective,
    SingleCheckboxWrapperComponent,
    RadioDirective,
    SingleRadioWrapperComponent,
  ],
  templateUrl: './kinder-filter.component.html',
  styleUrl: './kinder-filter.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KinderFilterComponent extends FilterSessionStorage<ErweiterteFilterJson> implements OnInit {
  readonly activeModal = inject(NgbActiveModal);
  readonly timetravelStore = inject(TimetravelStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly labelsStore = inject(LabelsStore);
  readonly impfstoffeChargenStore = inject(ImpfstoffeChargenStore);
  readonly anlassConfigOptionsStore = inject(AnlassConfigOptionsStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  readonly chargenList = computed(() => this.impfstoffeChargenStore.chargenList()
    .filter(charge => charge.data.aktiv));

  filterData = model.required<ErweiterteFilterJson>();

  readonly SESSIONSTORAGE_KEY = 'kinderFilterErweitert';

  notizStatus = Object.values(NotizStatus);
  krankheitTypes = Object.values(Krankheit);
  anlassStatus = Object.values(AnlassStatus);
  anlassArt = Object.values(AnlassArt);
  anlassTyp = Object.values(this.anlassConfigOptionsStore.getVisibleAnlassTypen());
  einladungStatus = Object.values(this.tenantSettingsStore.availableEinladungStatus());

  protected readonly compareFn = compareFn<LabelJson>;
  protected readonly DetailMode = DetailMode;

  form = new KinderFilterForm(inject(NonNullableFormBuilder), inject(DestroyRef));
  NOT_ASSIGNED = 'NOT_ASSIGNED';

  constructor() {
    super();

    effect(() => {
      const data = this.filterData();
      this.form.reset(data);
    });
  }

  ngOnInit(): void {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.form.reset(filter);
    }
  }

  cancel(): void {
    this.activeModal.dismiss('cancel');
  }

  async submit(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      this.setSessionStorageFilter(payload);
      console.log('submit', payload);
      this.activeModal.close(payload);
    }
  }

  displaySchuljahr(schuljahr: number | undefined) {
    if (isNullish(schuljahr)) {
      return undefined;
    }
    return schuljahr.toString() + '/' + (schuljahr + 1).toString().substring(2)
  }

  getSchuljahrOptions(): number[] | undefined {
    const now = fromISODateString(this.timetravelStore.now()) || new Date();
    const schuljahr = now.getFullYear();
    const options = [];

    if (isNullish(schuljahr)) {
      return undefined;
    }
    for (let jahr = schuljahr - 5; jahr <= schuljahr + 5; jahr++) {
      options.push(jahr);
    }
    return options;
  }

  mapSessionStorageStringToFilter(sessionStorageFilter: string): ErweiterteFilterJson | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as ErweiterteFilterJson;

      return ensurePropsFlat<ErweiterteFilterJson>({
        anlassPendent: safeValue(parsed.anlassPendent) ?? false,
        zeitraumVon: safeDate(parsed.zeitraumVon),
        zeitraumBis: safeDate(parsed.zeitraumBis),
        schuljahr: safeNumber(parsed.schuljahr),
        verantwortlicherId: safeId(parsed.verantwortlicherId),
        labels: Array.isArray(parsed.labels) ? parsed.labels : [],
        status: safeEnum(NotizStatus, parsed.status),
        inaktiv: !!parsed.inaktiv,
        mpaId: safeId(parsed.mpaId),
        arztId: safeId(parsed.arztId),
        dossierNachkontrolle: safeValue(parsed.dossierNachkontrolle),
        immunitaetVariezellen: safeValue(parsed.immunitaetVariezellen),
        immunitaetMasern: safeValue(parsed.immunitaetMasern),
        krankheit: safeEnum(Krankheit, parsed.krankheit),
        impfdatumVon: safeDate(parsed.impfdatumVon),
        impfdatumBis: safeDate(parsed.impfdatumBis),
        impfungCharge: safeId(parsed.impfungCharge),
        anlassZeitraumVon: safeDate(parsed.anlassZeitraumVon),
        anlassZeitraumBis: safeDate(parsed.anlassZeitraumBis),
        anlassSchuljahr: safeNumber(parsed.anlassSchuljahr),
        anlassStatus: safeEnum(AnlassStatus, parsed.anlassStatus),
        anlassArt: safeEnum(AnlassArt, parsed.anlassArt),
        anlassTyp: safeEnum(AnlassTyp, parsed.anlassTyp),
        anlassMpaId: safeId(parsed.anlassMpaId),
        anlassArztId: safeId(parsed.anlassArztId),
        anlassWeiterePerson: safeId(parsed.anlassWeiterePerson),
        anlassTeilnahmeStatus: safeEnum(EinladungStatus, parsed.anlassTeilnahmeStatus),
        anlassImpfungMassnahmeVormerkungFolgetermin: safeValue(parsed.anlassImpfungMassnahmeVormerkungFolgetermin),
        anlassNotAssigned: safeValue(parsed.anlassNotAssigned),
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }
}

function safeId<T>(value: T): BackendEntityId | undefined {
  return isValidBackendEntityId(value) ? value : undefined;
}

function safeEnum<E extends object, T>(enumType: E, value: T):  E[keyof E] | undefined {
  return isValidEnumValue(enumType, value) ? value : undefined;
}

function safeDate(value?: ISODateString): ISODateString | undefined {
  return isISODateString(value) ? value : undefined;
}

function safeNumber(value: unknown): number | undefined {
  if(isNullish(value) || (typeof value === "string" && value.trim() === '')){
    return undefined;
  }
  return Number.isFinite(Number(value)) ? undefined : Number(value);
}

function safeValue<T>(value: T): T | undefined {
  return isNotNullish(value) ? value : undefined;
}
