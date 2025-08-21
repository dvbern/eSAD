import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {ArtAbmeldung, EinladungJson, GrundAbmeldung} from '@dvb/data-access-backend';
import {BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireFilledIn, requireValue} from '@dvb/util-shared';

export interface EinladungAbmeldenJson {
  grundAbmeldung: GrundAbmeldung;
  artAbmeldung: ArtAbmeldung;
  bemerkungAbmeldung: string;
}

function makeForm(fb: NonNullableFormBuilder) {
  return fb.group({
    vorname: fb.control<string>({value: '', disabled: true}),
    name: fb.control<string>({value: '', disabled: true}),
    grundAbmeldung: fb.control<GrundAbmeldung | undefined>(undefined, Validators.required),
    artAbmeldung: fb.control<ArtAbmeldung | undefined>(undefined, Validators.required),
    bemerkungAbmeldung: fb.control<string>(''),
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
    });
  }

  valueOnValid(): EinladungAbmeldenJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<EinladungAbmeldenJson>({
      grundAbmeldung: requireFilledIn(value.grundAbmeldung),
      artAbmeldung: requireFilledIn(value.artAbmeldung),
      bemerkungAbmeldung: value.bemerkungAbmeldung,
    });
  }
}
