import {AnlassJson, AnlassSaveJson, AnlassTyp} from '@dvb/data-access-backend';
import {isISODateString, isISODateTimeString, ISODateTimeString} from '@dvb/data-access-types';
import {isNullish, Nullish} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';

export interface AnlassJsonEnhanced extends AnlassJson {
  fullTitle: string;
  dateTime?: ISODateTimeString
}

export function enhanceAnlass(anlass: AnlassJson, transloco: TranslocoService): AnlassJsonEnhanced {
  return {
    ...anlass,
    fullTitle: anlassTitle(anlass.data, transloco),
    dateTime: anlassDateTime(anlass.data),
  };
}

export function anlassTitleOptional(anlass: AnlassSaveJson | Nullish, transloco: TranslocoService): string | undefined {
  if (!anlass) {
    return undefined;
  }
  return anlassTitle(anlass, transloco);
}

export function anlassTitle(anlass: AnlassSaveJson, transloco: TranslocoService): string {
  return anlassTitleBasic(anlass.schuljahr, anlass.anlassTyp, anlass.titel, transloco);
}

export function anlassDateTime(anlass: AnlassSaveJson): ISODateTimeString | undefined {
  if (isISODateTimeString(anlass.zeitVon)) {
    return anlass.zeitVon;
  } else if (isISODateString(anlass.datum)) {
    return anlass.datum + 'T00:00:00Z' as ISODateTimeString;
  } else {
    return undefined;
  }
}

export function anlassTitleBasic(
  jahr: number | Nullish,
  anlassTyp: AnlassTyp | Nullish,
  anlassTitel: string,
  transloco: TranslocoService,
): string {
  return anlassTitlePrefix(jahr, anlassTyp, transloco) + ' ' + anlassTitel;
}

export function anlassTitlePrefix(
  jahr: number | Nullish,
  anlassTyp: AnlassTyp | Nullish,
  transloco: TranslocoService,
): string {
  const schuljahrString = jahr
    ? displaySchuljahr(jahr)
    : '<' + transloco.translate('anlass.detail.schuljahr') + '>';
  const anlassTypKuerzel = anlassTyp ?
    (transloco.translate('Anlass.title.prefix.AnlassTyp.' + anlassTyp)) :
    undefined;
  const anlassTypString = anlassTypKuerzel ?? '<' + transloco.translate('anlass.detail.typ') + '>';
  return [schuljahrString, anlassTypString].join(' ');
}

export function displaySchuljahr(schuljahr: number | undefined) {
  if (isNullish(schuljahr)) {
    return undefined;
  }
  return schuljahr.toString() + '/' + (schuljahr + 1).toString().substring(2);
}
