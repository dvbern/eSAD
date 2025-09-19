import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {ArtAbmeldung, EinladungJson, GrundAbmeldung} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireFilledIn, requireValue} from '@dvb/util-shared';

export interface EinladungAbmeldenJson {
  grundAbmeldung: GrundAbmeldung;
  artAbmeldung: ArtAbmeldung;
  bemerkungAbmeldung: string;
  versionAbmeldungsNotiz: number
}

function makeForm(fb: NonNullableFormBuilder) {
  return fb.group({
    vorname: fb.control<string>({value: '', disabled: true}),
    name: fb.control<string>({value: '', disabled: true}),
    grundAbmeldung: fb.control<GrundAbmeldung | undefined>(undefined, Validators.required),
    artAbmeldung: fb.control<ArtAbmeldung | undefined>(undefined, Validators.required),
    bemerkungAbmeldung: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    versionAbmeldungsNotiz: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class EinladungAbmeldenForm extends BaseForm<ReturnType<typeof makeForm>, EinladungAbmeldenJson> {
  constructor(fb: NonNullableFormBuilder) {
    super(() => makeForm(fb));
  }

  resetForm(einladung: EinladungJson | Nullish): void {
    this.group.reset({
      vorname: einladung?.kind.data.vorname,
      name: einladung?.kind.data.name,
      grundAbmeldung: einladung?.data.grundAbmeldung,
      artAbmeldung: einladung?.data.artAbmeldung,
      bemerkungAbmeldung: einladung?.data.bemerkungAbmeldung,
      versionAbmeldungsNotiz: einladung?.data.versionAbmeldungsNotiz
    });
  }

  valueOnValid(): EinladungAbmeldenJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<EinladungAbmeldenJson>({
      grundAbmeldung: requireFilledIn(value.grundAbmeldung),
      artAbmeldung: requireFilledIn(value.artAbmeldung),
      bemerkungAbmeldung: value.bemerkungAbmeldung,
      versionAbmeldungsNotiz: value.versionAbmeldungsNotiz ?? globalConstants.entityPrePersistVersion,
    });
  }
}
