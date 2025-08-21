import {FormGroup} from '@angular/forms';
import {AudiogrammJson, Dezibel} from '@dvb/data-access-backend';
import {ensureProps, Nullish} from '@dvb/util-shared';

export type AudiogrammKey = keyof AudiogrammJson;

export function getAllAudiogrammKeys(): AudiogrammKey[] {
  const audiogramm = ensureProps<AudiogrammJson>({
    dbHz250: Dezibel.DB20,
    dbHz500: Dezibel.DB20,
    dbHz1000: Dezibel.DB20,
    dbHz2000: Dezibel.DB20,
    dbHz4000: Dezibel.DB20,
    dbHz6000: Dezibel.DB20,
    dbHz8000: Dezibel.DB20,
  });
  return Object.keys(audiogramm) as AudiogrammKey[];
}

export function audiogrammKeyToFrequenz(key: AudiogrammKey): number {
  const match = RegExp(/dbHz(\d+)/).exec(key);
  if (match) {
    return parseInt(match[1], 10);
  }
  return 0;
}

export function toAudiogrammJson(formGroup: FormGroup): AudiogrammJson | Nullish {
  const audiogramm = {} as AudiogrammJson;
  const keys = getAllAudiogrammKeys();
  for (const key of keys) {
    audiogramm[key] = formGroup.controls[key]?.value;
  }
  if (Object.keys(audiogramm).length > 0) {
    return audiogramm;
  }
  return undefined;
}
