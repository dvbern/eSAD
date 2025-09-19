import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {InfomaterialJson, InformationEltern, UntersuchungSehenMassnahmenSaveJson,} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
}

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    bereitsBehandeltSehen: fb.control<boolean>(false),
    ueberweisungAugenarzt: fb.control<boolean>(false),
    nachkontrolleSehen: fb.control<boolean>(false),
    abgegebenesInfomaterial: fb.control<InfomaterialJson[]>([]),
    informationEltern: fb.control<InformationEltern | undefined>(undefined),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class UntersuchungSehenMassnahmenForm extends BaseForm<ReturnType<typeof makeForm>, UntersuchungSehenMassnahmenSaveJson> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: UntersuchungSehenMassnahmenSaveJson | Nullish): void {
    this.group.reset({
      ...value,
      bereitsBehandeltSehen: value?.bereitsBehandeltSehen,
      ueberweisungAugenarzt: value?.ueberweisungAugenarzt,
      nachkontrolleSehen: value?.nachkontrolleSehen,
      abgegebenesInfomaterial: value?.abgegebenesInfomaterial,
      informationEltern: value?.informationEltern,
      version: value?.version,
    });
  }

  public override valueOnValid(): UntersuchungSehenMassnahmenSaveJson {
    const value = requireValue(this.group.getRawValue());
    // @ts-expect-error scheint er nicht zu erkennen
    const abgegebenesInfomaterialNotNull: OptionalToUndefined<InfomaterialJson>[] = value.abgegebenesInfomaterial ?? [];
    return ensureProps<UntersuchungSehenMassnahmenSaveJson>({
      bereitsBehandeltSehen: value.bereitsBehandeltSehen,
      abgegebenesInfomaterial: abgegebenesInfomaterialNotNull,
      informationEltern: value.informationEltern,
      ueberweisungAugenarzt: value.ueberweisungAugenarzt,
      nachkontrolleSehen: value.nachkontrolleSehen,
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }
}
