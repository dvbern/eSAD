import {ImpfstoffChargeJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {Nullish} from '@dvb/util-shared';
import {getChargeOptions} from './impfstoffe-chargen.store';

function charge(name: string, aktiv: boolean): ImpfstoffChargeJson {
  return {
    id: name as BackendEntityId,
    impfstoffId: 'impfstoff-1' as BackendEntityId,
    data: {
      name,
      aktiv,
    },
  } as ImpfstoffChargeJson;
}

const aktiv1 = charge('Charge 1', true);
const aktiv2 = charge('Charge 2', true);

const option1 = {label: 'Charge 1', inaktiv: false};
const option2 = {label: 'Charge 2', inaktiv: false};

describe('getChargeOptions', () => {
  it.each([
    {
      description: 'returns an option per active charge',
      activeChargen: [aktiv1, aktiv2],
      currentChargeName: undefined as string | Nullish,
      expected: [option1, option2],
    },
    {
      description: 'appends the current charge when it is not active',
      activeChargen: [aktiv1, aktiv2],
      currentChargeName: 'Charge inaktiv',
      expected: [option1, option2, {label: 'Charge inaktiv', inaktiv: true}],
    },
    {
      description: 'does not duplicate the current charge when it is active',
      activeChargen: [aktiv1, aktiv2],
      currentChargeName: 'Charge 1',
      expected: [option1, option2],
    },
    {
      description: 'returns only the current charge when no charge is active',
      activeChargen: [] as ImpfstoffChargeJson[],
      currentChargeName: 'Charge adhoc',
      expected: [{label: 'Charge adhoc', inaktiv: true}],
    },
    {
      description: 'returns an empty list without active chargen and without current charge',
      activeChargen: [] as ImpfstoffChargeJson[],
      currentChargeName: null,
      expected: [],
    },
  ])(
    '$description',
    ({activeChargen, currentChargeName, expected}) => {
      const result = getChargeOptions(activeChargen, currentChargeName);

      expect(result)
        .toEqual(expected);
    },
  );
});
