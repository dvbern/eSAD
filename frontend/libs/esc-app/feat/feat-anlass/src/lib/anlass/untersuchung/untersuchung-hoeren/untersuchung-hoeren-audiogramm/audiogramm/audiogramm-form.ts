import {DestroyRef, Signal} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {AnlassConfigJson, AudiogrammJson, Dezibel, RequirementSetting} from '@dvb/data-access-backend';
import {BaseForm} from '@dvb/util-angular';
import {Nullish} from '@dvb/util-shared';
import {AudiogrammBothJson} from './audiogramm-both-json';
import {AudiogrammKey, toAudiogrammJson} from './audiogramm-util';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  frequenzenKeys: AudiogrammKey[],
  anlassConfigSig: Signal<AnlassConfigJson | undefined>
}

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    links: fb.group({}),
    rechts: fb.group({}),
  });
  config.frequenzenKeys.forEach(frequenz => {
    group.controls.links.addControl('' + frequenz, fb.control<Dezibel | undefined>(undefined));
    group.controls.rechts.addControl('' + frequenz, fb.control<Dezibel | undefined>(undefined));
  });
  return group;
}

export class AudiogrammForm extends BaseForm<ReturnType<typeof makeForm>, AudiogrammBothJson> {

  constructor(protected _config: Config) {
    super(() => makeForm(_config));
  }

  get config(): Config {
    return this._config;
  }

  resetForm(data: AudiogrammBothJson | Nullish): void {
    this.group.reset();
    if (data) {
      const links: AudiogrammJson | Nullish = data.links;
      const rechts: AudiogrammJson | Nullish = data.rechts;
      this.config.frequenzenKeys.forEach(key => {
        if (links) {
          // @ts-expect-error dynamische Forms...
          this.group.controls.links.controls[key].reset(links[key]);
        }
        if (rechts) {
          // @ts-expect-error dynamische Forms...
          this.group.controls.rechts.controls[key].reset(rechts[key]);
        }
      });
    }
  }

  valueOnValid(): AudiogrammBothJson {
    const audiogrammLinks = toAudiogrammJson(this.group.controls.links);
    const audiogrammRechts = toAudiogrammJson(this.group.controls.rechts);
    return {
      links: audiogrammLinks,
      rechts: audiogrammRechts,
    };
  }

  setValidators(anlassConfig: AnlassConfigJson, nichtGeprueftIsChecked: boolean): void {
    // Audiogramm 250Hz
    if (anlassConfig.hoerenAudiogramm250Hz === RequirementSetting.REQUIRED) {
      this.addRequiredValidators('dbHz250');
    } else {
      this.removeValidators('dbHz250');
    }
    // Audiogramm 500Hz bis 6000Hz
    if (anlassConfig.hoerenAudiogramm === RequirementSetting.REQUIRED) {
      this.config.frequenzenKeys
          .filter(frequenz => frequenz !== 'dbHz250' && frequenz !== 'dbHz8000')
          .forEach(frequenz => {
            this.addRequiredValidators(frequenz);
          });
    } else {
      this.config.frequenzenKeys
          .filter(frequenz => frequenz !== 'dbHz250' && frequenz !== 'dbHz8000')
          .forEach(frequenz => {
            this.removeValidators(frequenz);
          });
    }
    // Audiogramm 8000Hz
    if (anlassConfig.hoerenAudiogramm8000Hz === RequirementSetting.REQUIRED && !nichtGeprueftIsChecked) {
      this.addRequiredValidators('dbHz8000');
    } else {
      this.removeValidators('dbHz8000');
    }
    if (nichtGeprueftIsChecked) {
      this.config.frequenzenKeys.forEach(frequenz => {
        this.removeValidators(frequenz);
      });
    }
  }

  private removeValidators(frequenz: AudiogrammKey): void {
    // @ts-expect-error dynamische Forms...
    this.group.controls.rechts.controls[frequenz].removeValidators(Validators.required);
    // @ts-expect-error dynamische Forms...
    this.group.controls.rechts.controls[frequenz].updateValueAndValidity();
    // @ts-expect-error dynamische Forms...
    this.group.controls.links.controls[frequenz].removeValidators(Validators.required);
    // @ts-expect-error dynamische Forms...
    this.group.controls.links.controls[frequenz].updateValueAndValidity();
  }

  private addRequiredValidators(frequenz: AudiogrammKey): void {
    // @ts-expect-error dynamische Forms...
    this.group.controls.rechts.controls[frequenz].setValidators(Validators.required);
    // @ts-expect-error dynamische Forms...
    this.group.controls.rechts.controls[frequenz].updateValueAndValidity();
    // @ts-expect-error dynamische Forms...
    this.group.controls.links.controls[frequenz].setValidators(Validators.required);
    // @ts-expect-error dynamische Forms...
    this.group.controls.links.controls[frequenz].updateValueAndValidity();
  }
}
