import {ChangeDetectionStrategy, Component, computed, input} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Krankheit} from '@dvb/data-access-backend';
import {IconComponent, TableControlComponent, TextInputDirective} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';
import {UntersuchungImpfstatusSubFormType} from '../untersuchung-impfstatus-list.form';

@Component({
    selector: 'feat-untersuchung-impfstatus-list',
    templateUrl: './untersuchung-impfstatus-list.component.html',
    styleUrl: './untersuchung-impfstatus-list.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
    FormsModule,
    ReactiveFormsModule,
    TextInputDirective,
    IconComponent,
    TranslocoPipe,
    TableControlComponent
]
})
export class UntersuchungImpfstatusListComponent {

  krankheitenLeft = input.required<Krankheit[]>();
  krankheitenRight = input.required<Krankheit[]>();
  columns = computed(() => [
    ...(this.krankheitenLeft().length ? [this.krankheitenLeft()] : []),
    ...(this.krankheitenRight().length ? [this.krankheitenRight()] : []),
  ]);

  public impfentscheideFormGroup = input.required<FormGroup>();

  #maxAnzahlKrankheitenProSeite =
    computed(() => Math.max(this.krankheitenLeft().length, this.krankheitenRight().length));

  /**
   * Die Anzahl Rows muss zur Runtime berechnet werden da die Anzahl Krankheiten bei jedem Anlass anders sein kann.
   */
  gridRowValue = computed(() => {
    const defaultRows = 2;  // 1x TableHeader;
                            // 1x Es muss bis nach der letzten Row reichen und daher muss bei 6 Rows 7 angegeben werden.
    return `${defaultRows} / ${this.#maxAnzahlKrankheitenProSeite() + defaultRows}`;
  });

  controlForKrankheit(krankheit: Krankheit, attribute: string): FormControl<unknown> {
    // @ts-expect-error der Typ ist kompliziert, aber korrekt
    const krankheitFormGroup: UntersuchungImpfstatusSubFormType = this.impfentscheideFormGroup().controls[krankheit];
    // @ts-expect-error der Typ ist kompliziert, aber korrekt
    return krankheitFormGroup.controls[attribute];
  }

  statusForKrankheit(krankheit: Krankheit): 'default' | 'impfschutzGenuegend' | 'impfempfehlung' | 'impfentscheid' {
    if (this.controlForKrankheit(krankheit, 'impfentscheid')?.value) {
      return 'impfentscheid';
    }
    if (this.controlForKrankheit(krankheit, 'impfempfehlung')?.value) {
      return 'impfempfehlung';
    }
    if (this.controlForKrankheit(krankheit, 'impfschutzGegeben')?.value) {
      return 'impfschutzGenuegend';
    }
    return 'default';
  }
}


