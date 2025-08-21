import {Geschlecht} from '@dvb/data-access-backend';
import {IconUseCase} from '@dvb/ui-components';
import {isNullish} from '@dvb/util-shared';

export function getUseCaseGechlecht(geschlecht: Geschlecht | undefined): IconUseCase {
  if (isNullish(geschlecht)) {
    return 'sex_unknown';
  }

  switch (geschlecht) {
    case 'MAENNLICH':
      return 'male';
    case 'WEIBLICH':
      return 'female';
    case 'DIVERS':
      return 'divers';
    case 'UNBEKANNT':
      return 'sex_unknown';
  }
}

export function uniqueFilter(value: string | undefined, index: number, self: (string | undefined)[]) {
  return self.indexOf(value) === index;
}
