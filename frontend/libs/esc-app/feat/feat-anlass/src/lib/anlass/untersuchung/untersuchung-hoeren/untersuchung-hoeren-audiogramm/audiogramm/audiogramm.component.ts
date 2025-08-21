import {ChangeDetectionStrategy, Component, computed, input} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Dezibel, RequirementSetting} from '@dvb/data-access-backend';
import {TranslocoPipe} from '@jsverse/transloco';
import {AudiogrammForm} from './audiogramm-form';
import {AudiogrammKey, audiogrammKeyToFrequenz} from './audiogramm-util';

@Component({
  selector: 'feat-audiogramm',
  templateUrl: './audiogramm.component.html',
  styleUrl: './audiogramm.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    TranslocoPipe,
  ],
})
export class AudiogrammComponent {

  form = input.required<AudiogrammForm>();

  frequenzenValues = computed(() => {
    let frequenzen = this.form().config.frequenzenKeys;
    const anlassConfig = this.form().config.anlassConfigSig();

    if (anlassConfig?.hoerenAudiogramm250Hz === RequirementSetting.NOT_VISIBLE) {
      frequenzen = frequenzen.filter(freq => freq !== 'dbHz250');
    }
    if (anlassConfig?.hoerenAudiogramm === RequirementSetting.NOT_VISIBLE) {
      frequenzen =
          frequenzen.filter(freq => ![
            'dbHz500',
            'dbHz1000',
            'dbHz2000',
            'dbHz4000',
            'dbHz6000',
          ].includes(freq));
    }
    if (anlassConfig?.hoerenAudiogramm8000Hz === RequirementSetting.NOT_VISIBLE) {
      frequenzen = frequenzen.filter(freq => freq !== 'dbHz8000');
    }
    return frequenzen;
  });

  dezibelValues = Object.values(Dezibel);

  public getControl(group: FormGroup, freq: AudiogrammKey): FormControl {
    // @ts-expect-error der Typ ist kompliziert, aber korrekt
    return group.controls[freq];
  }

  getFrequenzGridPosition(index: number, side: 'links' | 'rechts'): number {
    const rightOffset = 1;
    const leftOffset = 10;
    return side === 'rechts' ? rightOffset + index + 1 : leftOffset + index;
  }

  protected readonly audiogrammKeyToFrequenz = audiogrammKeyToFrequenz;
}


