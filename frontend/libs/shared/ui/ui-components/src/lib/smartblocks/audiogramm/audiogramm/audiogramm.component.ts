import {NgIf} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, input} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TranslocoPipe} from '@jsverse/transloco';
import {AudiogrammFormType} from '../audiogramm-form';
import {AudiogrammFrequenz, AudiogrammValue} from '../json/audiogramm-json';

@Component({
    selector: 'svui-audiogramm',
    templateUrl: './audiogramm.component.html',
    styleUrl: './audiogramm.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        FormsModule,
        ReactiveFormsModule,
        NgIf,
        TranslocoPipe,
    ]
})
export class AudiogrammComponent {

  frequenzen = input.required<AudiogrammFrequenz[]>();
  values = input.required<AudiogrammValue[]>();
  columns = computed(() => [
    {label: 'audiogramm.seite.rechts', group: this.audiogrammFormGroup().controls.rechts},
    {label: 'audiogramm.seite.links', group: this.audiogrammFormGroup().controls.links},
  ]);

  public audiogrammFormGroup = input.required<AudiogrammFormType>();

  public getControl(group: FormGroup, freq: AudiogrammFrequenz): FormControl {
    // @ts-expect-error der Typ ist kompliziert, aber korrekt
    return group.controls[freq];
  }
}


