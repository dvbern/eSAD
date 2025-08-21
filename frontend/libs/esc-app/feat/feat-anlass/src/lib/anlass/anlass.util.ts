import {AnlassJson, AnlassSaveJson, AnlassTyp} from '@dvb/data-access-backend';
import {isNullish, Nullish} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';

export interface AnlassJsonEnhanced extends AnlassJson {
  fullTitle: string;
}

export function enhanceAnlass(anlass: AnlassJson, transloco: TranslocoService): AnlassJsonEnhanced {
  return {
    ...anlass,
    fullTitle: anlassTitle(anlass.data, transloco),
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

  const prefix = [schuljahrString, anlassTypString].join(' ');
  return prefix;
}

export function displaySchuljahr(schuljahr: number | undefined) {
  if (isNullish(schuljahr)) {
    return undefined;
  }
  return schuljahr.toString() + '/' + (schuljahr + 1).toString().substring(2);
}
