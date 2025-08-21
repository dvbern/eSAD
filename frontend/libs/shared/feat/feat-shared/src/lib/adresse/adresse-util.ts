import {AdresseJson} from '@dvb/data-access-backend';
import {isNullish} from '@dvb/util-shared';

export function getAdresse(adresse: AdresseJson | undefined): string {
  if (isNullish(adresse)) {
    return '';
  }
  const strasseUndNr = adresse.strasseUndNr ? adresse.strasseUndNr + ', ' : '';
  return `${strasseUndNr} ${adresse?.plz ?? ''} ${adresse.ort ?? ''}`;
}
