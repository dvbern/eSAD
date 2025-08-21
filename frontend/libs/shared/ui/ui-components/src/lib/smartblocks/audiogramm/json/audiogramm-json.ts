// TODO ESC-178_X Json in Java/API erstellen und generieren, dann hier löschen

export interface AudiogrammJson {
  links: Record<AudiogrammFrequenz, AudiogrammValue | undefined> | undefined;
  rechts: Record<AudiogrammFrequenz, AudiogrammValue | undefined> | undefined;
}

export const AudiogrammFrequenz = [
  '250',
  '500',
  '1000',
  '2000',
  '4000',
  '6000',
  '8000',
] as const;

export type AudiogrammFrequenz = typeof AudiogrammFrequenz[number];

export const AudiogrammValue = [
  '10',
  '20',
  '30',
  '40',
  '50',
  '60',
] as const;

export type AudiogrammValue = typeof AudiogrammValue[number];
