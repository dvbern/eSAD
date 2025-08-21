import {AudiogrammJson} from '@dvb/data-access-backend';
import {Nullish} from '@dvb/util-shared';

export class AudiogrammBothJson {
  links: AudiogrammJson | Nullish;
  rechts: AudiogrammJson | Nullish;
}
