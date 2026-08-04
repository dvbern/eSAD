import {AnlassJson, AnlassSaveJson, AnlassTyp} from '@dvb/data-access-backend';
import {isISODateString, isISODateTimeString, ISODateTimeString} from '@dvb/data-access-types';
import {isNotNullish, isNullish, Nullish} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {AnlassJsonEnhanced} from "@dvb/feat-shared";

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
  const prefix = anlassTitlePrefix(anlass.schuljahr, anlass.anlassTyp, transloco);
  if (isNullish(anlass.titel)) {
    return prefix;
  }
  return [prefix, anlass.titel].join(' ');
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

export function anlassTitlePrefix(
  jahr: number | Nullish,
  anlassTyp: AnlassTyp | Nullish,
  transloco: TranslocoService,
): string {
  const schuljahrString = isNotNullish(jahr) ? `${jahr}/${String(jahr + 1).slice(-2)}` : undefined;
  const anlassTypKuerzel = anlassTyp ? (transloco.translate('Anlass.title.prefix.AnlassTyp.' + anlassTyp)) : undefined;
  return [
    schuljahrString ?? `<${transloco.translate('anlass.detail.schuljahr')}>`,
    anlassTypKuerzel ?? `<${transloco.translate('anlass.detail.typ')}>`
  ].join(' ');
}
