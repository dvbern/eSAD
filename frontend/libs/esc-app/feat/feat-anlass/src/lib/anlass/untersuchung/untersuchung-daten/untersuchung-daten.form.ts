import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassConfigJson,
  BenutzerOptionJson,
  Geschlecht,
  Klassenstufe,
  Land,
  LandCode,
  UntersuchungDurchfuehrungDataSaveJson,
  UntersuchungDurchfuehrungSaveJson,
  UntersuchungKindDatenJson,
  UntersuchungKindDatenSaveJson,
} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm, DetailMode} from '@dvb/util-angular';
import {ensureProps, Nullish, requireFilledIn, requireValue} from '@dvb/util-shared';

function makeForm(fb: NonNullableFormBuilder) {
  const maxLength = AppValidators.dbMaxLength();
  return fb.group({
    name: fb.control<string>({value: '', disabled: true}, [Validators.required, maxLength]),
    vorname: fb.control<string>({value: '', disabled: true}, [Validators.required, maxLength]),
    esadId: fb.control<string>({value: '', disabled: true}),
    geschlecht: fb.control<Geschlecht | undefined>({value: undefined, disabled: true}, Validators.required),
    geburtsdatum: fb.control<ISODateString | undefined>({value: undefined, disabled: true}, Validators.required),
    alterDuodezimal: fb.control<string>({value: '', disabled: true}),
    nationalitaet: fb.control<Land | undefined>({value: undefined, disabled: true}),
    landCode: fb.control<LandCode | undefined>({value: undefined, disabled: true}),
    schulhaus: fb.control<string>({value: '', disabled: true}, [Validators.required, maxLength]),
    quartier: fb.control<string>({value: '', disabled: true}, maxLength),
    schulkreis: fb.control<string>({value: '', disabled: true}, maxLength),
    schulhausnummer: fb.control<string>({value: '', disabled: true},
      [AppValidators.numberOrNumericChars(), Validators.maxLength(3)]),
    klassenstufe: fb.control<Klassenstufe | undefined>({value: undefined, disabled: true}),
    klasse: fb.control<string>({value: '', disabled: true}, maxLength),
    klassentyp: fb.control<string>({value: '', disabled: true}, maxLength),
    anlassDatum: fb.control<ISODateString | undefined>({value: undefined, disabled: true}),
    arzt: fb.control<BenutzerOptionJson | undefined>(undefined),
    mpa: fb.control<BenutzerOptionJson | undefined>(undefined),
    nachkontrolle: fb.control<boolean>(false),
    nachzuegler: fb.control<boolean>(false),
    neueintritt: fb.control<boolean>(false),
    versionDurchfuehrung: fb.control<number | Nullish>({value: undefined, disabled: true}),
    versionKindDaten: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class UntersuchungDatenForm extends BaseForm<ReturnType<typeof makeForm>, UntersuchungDurchfuehrungSaveJson> {
  constructor(fb: NonNullableFormBuilder) {
    super(() => makeForm(fb));
  }

  setValidators(config: AnlassConfigJson): void {
    if (config.datenUntersuchung === 'REQUIRED') {
      this.group.controls.arzt.addValidators(Validators.required);
      this.group.controls.mpa.addValidators(Validators.required);
    }
  }

  resetForm(
    durchfuehrungDaten: UntersuchungDurchfuehrungSaveJson | undefined,
    kindDaten: UntersuchungKindDatenJson | undefined,
    mode: DetailMode,
  ): void {
    super.reset(durchfuehrungDaten, mode);
    this.group.reset({
      ...durchfuehrungDaten,
      ...kindDaten?.data,
      ...kindDaten,
    });
    // Wir haben zwei SaveJsons mit Versionsnummer in diesem Form, darum muss dies
    // manuell gemappt werden
    this.group.controls.versionDurchfuehrung.reset(durchfuehrungDaten?.version);
    this.group.controls.versionKindDaten.reset(kindDaten?.data?.version);
  }

  override valueOnValid(): UntersuchungDurchfuehrungSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<UntersuchungDurchfuehrungSaveJson>({
      arzt: value.arzt,
      mpa: value.mpa,
      nachkontrolle: value.nachkontrolle,
      nachzuegler: value.nachzuegler,
      neueintritt: value.neueintritt,
      version: value.versionDurchfuehrung ?? globalConstants.entityPrePersistVersion,
    });
  }

  // Wenn der Anlass abgeschlossen ist, kann ein Administrator zusätzlich noch die weggespeicherten Kinder Daten
  // anpassen.
  valueOnValidDurchfuehrungKind(): UntersuchungDurchfuehrungDataSaveJson {
    const values = this.group.getRawValue();
    return {
      untersuchungDurchfuehrung: this.valueOnValid(),
      kindDaten: ensureProps<UntersuchungKindDatenSaveJson>({
        name: requireFilledIn(values.name),
        vorname: requireFilledIn(values.vorname),
        geschlecht: requireFilledIn(values.geschlecht),
        geburtsdatum: requireFilledIn(values.geburtsdatum),
        nationalitaet: values.nationalitaet,
        schulhaus: values.schulhaus,
        quartier: values.quartier,
        schulkreis: values.schulkreis,
        schulhausnummer: values.schulhausnummer,
        klassenstufe: values.klassenstufe,
        klassentyp: values.klassentyp,
        klasse: values.klasse,
        version: values.versionKindDaten ?? globalConstants.entityPrePersistVersion,
      }),
    }
  }

  public setDisabledForm(notAllowed: boolean, isAbgeschlossen: boolean): void {
    if (notAllowed) {
      this.disableModifiableFields();
    } else {
      this.enableModifiableFields();
    }
    if (!notAllowed && isAbgeschlossen) {
      this.enableKindFields();
    } else {
      this.disableKindFields();
    }
  }

  private disableModifiableFields(): void {
    this.group.controls.arzt.disable();
    this.group.controls.mpa.disable();
    this.group.controls.nachkontrolle.disable();
    this.group.controls.nachzuegler.disable();
    this.group.controls.neueintritt.disable();
  }

  private enableModifiableFields(): void {
    this.group.controls.arzt.enable();
    this.group.controls.mpa.enable();
    this.group.controls.nachkontrolle.enable();
    this.group.controls.nachzuegler.enable();
    this.group.controls.neueintritt.enable();
  }

  private disableKindFields(): void {
    this.group.controls.name.disable();
    this.group.controls.vorname.disable();
    this.group.controls.geschlecht.disable();
    this.group.controls.geburtsdatum.disable();
    this.group.controls.nationalitaet.disable();
    this.group.controls.schulhaus.disable();
    this.group.controls.quartier.disable();
    this.group.controls.schulkreis.disable();
    this.group.controls.schulhausnummer.disable();
    this.group.controls.klassenstufe.disable();
    this.group.controls.klasse.disable();
    this.group.controls.klassentyp.disable();
  }

  private enableKindFields(): void {
    this.group.controls.name.enable();
    this.group.controls.vorname.enable();
    this.group.controls.geschlecht.enable();
    this.group.controls.geburtsdatum.enable();
    this.group.controls.nationalitaet.enable();
    this.group.controls.schulhaus.enable();
    this.group.controls.quartier.enable();
    this.group.controls.schulkreis.enable();
    this.group.controls.schulhausnummer.enable();
    this.group.controls.klassenstufe.enable();
    this.group.controls.klasse.enable();
    this.group.controls.klassentyp.enable();
  }
}
