import {FormGroup} from '@angular/forms';
import {AudiogrammSaveJson, Dezibel} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {ensureProps, Nullish} from '@dvb/util-shared';

export type AudiogrammKey = keyof Omit<AudiogrammSaveJson, 'version'>;

export function getAllAudiogrammKeys(): AudiogrammKey[] {
  const audiogramm = ensureProps<AudiogrammSaveJson>({
    dbHz250: Dezibel.DB20,
    dbHz500: Dezibel.DB20,
    dbHz1000: Dezibel.DB20,
    dbHz2000: Dezibel.DB20,
    dbHz4000: Dezibel.DB20,
    dbHz6000: Dezibel.DB20,
    dbHz8000: Dezibel.DB20,
    version: 0, // Version is not part of the keys, but we need it for ensureProps. will be removed later
  });
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const audiogrammWithoutVersion = audiogramm as any;
  delete audiogrammWithoutVersion.version;
  return Object.keys(audiogrammWithoutVersion) as AudiogrammKey[];
}

export function audiogrammKeyToFrequenz(key: AudiogrammKey): number {
  const match = RegExp(/dbHz(\d+)/).exec(key);
  if (match) {
    return parseInt(match[1], 10);
  }
  return 0;
}

export function toAudiogrammJson(formGroup: FormGroup): AudiogrammSaveJson | Nullish {
  const audiogramm = {} as AudiogrammSaveJson;
  const keys = getAllAudiogrammKeys();
  for (const key of keys) {
    audiogramm[key] = formGroup.controls[key]?.value;
  }
  audiogramm['version'] = formGroup.controls['version']?.value ?? globalConstants.entityPrePersistVersion;
  if (Object.keys(audiogramm).length > 0) {
    return audiogramm;
  }
  return undefined;
}
