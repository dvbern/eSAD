import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {InfomaterialJson, InformationEltern, UntersuchungMassnahmenSaveJson} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, ensurePropsFlat, Nullish, requireValue} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    keineMassnahmen: fb.control<boolean>(false),
    beratungMPA: fb.control<boolean>(false),
    beratungSchulAerztin: fb.control<boolean>(false),
    anderes: fb.control<boolean>(false),
    anderesBemerkung: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    bereitsBehandeltAnderes: fb.control<boolean>(false),
    bereitsBehandeltAnderesBemerkung: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    bereitsBehandeltGroesseGewicht: fb.control<boolean>(false),
    bereitsBehandeltHoeren: fb.control<boolean>(false),
    bereitsBehandeltMotorik: fb.control<boolean>(false),
    bereitsBehandeltSehen: fb.control<boolean>(false),
    bereitsBehandeltSprache: fb.control<boolean>(false),
    abgegebenesInfomaterial: fb.control<InfomaterialJson[]>([]),
    informationEltern: fb.control<InformationEltern | undefined>(undefined),
    ueberweisungAugenarzt: fb.control<boolean>(false),
    ueberweisungHausarzt: fb.control<boolean>(false),
    nachkontrolleSehen: fb.control<boolean>(false),
    nachkontrolleHoeren: fb.control<boolean>(false),
    nachkontrolleGroesseGewicht: fb.control<boolean>(false),
    nachkontrolleSprache: fb.control<boolean>(false),
    nachkontrolleMotorik: fb.control<boolean>(false),
    nachkontrolleAnderes: fb.control<boolean>(false),
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });

  group.controls.anderes.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: anderes => {
      if (!anderes) {
        group.controls.anderesBemerkung.reset();
      }
    },
    error: console.error,
  });

  group.controls.bereitsBehandeltAnderes.valueChanges
    .pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: anderes => {
        if (!anderes) {
          group.controls.bereitsBehandeltAnderesBemerkung.reset();
        }
      },
      error: console.error,
    });

  return group;
}

export class UntersuchungMassnahmenForm extends BaseForm<ReturnType<typeof makeForm>, UntersuchungMassnahmenSaveJson> {
  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: UntersuchungMassnahmenSaveJson | Nullish): void {
    this.group.reset({
      ...value,
      nachkontrolleSehen: value?.nachkontrolle.nachkontrolleSehen,
      nachkontrolleHoeren: value?.nachkontrolle.nachkontrolleHoeren,
      nachkontrolleGroesseGewicht: value?.nachkontrolle.nachkontrolleGroesseGewicht,
      nachkontrolleSprache: value?.nachkontrolle.nachkontrolleSprache,
      nachkontrolleMotorik: value?.nachkontrolle.nachkontrolleMotorik,
      nachkontrolleAnderes: value?.nachkontrolle.nachkontrolleAnderes,
    });
  }

  public override valueOnValid(): UntersuchungMassnahmenSaveJson {
    const value = requireValue(this.group.getRawValue());
    const saveJson = ensureProps<Omit<UntersuchungMassnahmenSaveJson, 'abgegebenesInfomaterial'>>({
      keineMassnahmen: value.keineMassnahmen,
      beratungMPA: value.beratungMPA,
      beratungSchulAerztin: value.beratungSchulAerztin,
      anderes: value.anderes,
      bereitsBehandeltAnderes: value.bereitsBehandeltAnderes,
      bereitsBehandeltAnderesBemerkung: value.bereitsBehandeltAnderesBemerkung,
      anderesBemerkung: value.anderesBemerkung,
      bereitsBehandeltGroesseGewicht: value.bereitsBehandeltGroesseGewicht,
      bereitsBehandeltHoeren: value.bereitsBehandeltHoeren,
      bereitsBehandeltMotorik: value.bereitsBehandeltMotorik,
      bereitsBehandeltSehen: value.bereitsBehandeltSehen,
      bereitsBehandeltSprache: value.bereitsBehandeltSprache,
      informationEltern: value.informationEltern,
      ueberweisungAugenarzt: value.ueberweisungAugenarzt,
      ueberweisungHausarzt: value.ueberweisungHausarzt,
      nachkontrolle: {
        nachkontrolleSehen: value.nachkontrolleSehen,
        nachkontrolleHoeren: value.nachkontrolleHoeren,
        nachkontrolleGroesseGewicht: value.nachkontrolleGroesseGewicht,
        nachkontrolleSprache: value.nachkontrolleSprache,
        nachkontrolleMotorik: value.nachkontrolleMotorik,
        nachkontrolleAnderes: value.nachkontrolleAnderes,
      },
      bemerkungen: value.bemerkungen,
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });

    return ensurePropsFlat<UntersuchungMassnahmenSaveJson>({
      ...saveJson,
      // nested langague props in TranslatedStringJson are optional, ensureProps can't be used
      abgegebenesInfomaterial: value.abgegebenesInfomaterial
    });
  }
}
