import {AudiogrammSaveJson} from '@dvb/data-access-backend';
import {Nullish} from '@dvb/util-shared';

export class AudiogrammBothJson {
  links: AudiogrammSaveJson | Nullish;
  rechts: AudiogrammSaveJson | Nullish;
}
