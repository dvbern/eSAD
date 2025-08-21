import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {whenFormValid} from '@dvb/util-angular';
import {Nullish} from '@dvb/util-shared';
import {AudiogrammFrequenz, AudiogrammJson, AudiogrammValue} from './json/audiogramm-json';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  frequenzen: AudiogrammFrequenz[]
}

function makeForm(config: Config) {
  const fb = config.fb;

  const group = fb.group({
    links: fb.group({}),
    rechts: fb.group({}),
  });
  config.frequenzen.forEach(frequenz => {
    group.controls.links.addControl('' + frequenz, fb.control<AudiogrammValue | null>(null));
    group.controls.rechts.addControl('' + frequenz, fb.control<AudiogrammValue | null>(null));
  });

  return group;
}

export class AudiogrammForm {

  group: ReturnType<typeof makeForm>;
  config: Config;

  constructor(config: Config) {
    this.config = config;
    this.group = makeForm(config);
  }

  valueOnValid(): AudiogrammJson {

    console.log(this.group.value);

    const links = {};
    const rechts = {};

    this.config.frequenzen.forEach(frequenz => {
      // @ts-expect-error dynamische Forms...
      links[frequenz] = this.group.controls.links.controls[frequenz].value;
      // @ts-expect-error dynamische Forms...
      rechts[frequenz] = this.group.controls.rechts.controls[frequenz].value;
    });

    // @ts-expect-error dynamische Forms...
    const newValue: AudiogrammJson = {links: links, rechts: rechts};

    return newValue;
  }

  reset(data: AudiogrammJson | Nullish): void {
    this.group.reset();

    this.config.frequenzen.forEach(frequenz => {

      const dataLinksValue = data?.links ? data.links[frequenz] : undefined;
      const dataRechtsValue = data?.rechts ? data.rechts[frequenz] : undefined;

      // @ts-expect-error dynamische Forms...
      this.group.controls.links.controls[frequenz].reset(dataLinksValue);
      // @ts-expect-error dynamische Forms...
      this.group.controls.rechts.controls[frequenz].reset(dataRechtsValue);

    });

  }

  async validate(): Promise<boolean> {
    return await whenFormValid(this.group, this.config.destroyRef);
  }

}

export type AudiogrammFormType = ReturnType<typeof makeForm>;
