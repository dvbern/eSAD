import {ChangeDetectionStrategy, Component, computed, effect, input, signal} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors} from '@angular/forms';
import {Krankheit} from '@dvb/data-access-backend';
import {FormErrorMessageComponent} from '@dvb/ui-components';
import {Nullish} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
    selector: 'feat-untersuchung-impfstatus-nur-unvollstaendige-list',
    templateUrl: './untersuchung-impfstatus-nur-unvollstaendige-list.component.html',
    styleUrl: './untersuchung-impfstatus-nur-unvollstaendige-list.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    TranslocoPipe,
    FormErrorMessageComponent
  ]
})
export class UntersuchungImpfstatusNurUnvollstaendigeListComponent {

  krankheitenLeft = input.required<Krankheit[]>();
  krankheitenRight = input.required<Krankheit[]>();
  columns = computed(() => [
    ...(this.krankheitenLeft().length ? [this.krankheitenLeft()] : []),
    ...(this.krankheitenRight().length ? [this.krankheitenRight()] : []),
  ]);

  public impfentscheideFormGroup = input.required<FormGroup>();

  // Fehler der impfentscheide-Group reaktiv abbilden, damit die Validierungsmeldung auch unter OnPush
  // sofort (direkt beim Auswaehlen von "Nein") und nicht erst beim Speichern angezeigt wird.
  readonly #errors = signal<ValidationErrors | Nullish>(null);
  readonly errors = this.#errors.asReadonly();

  #maxAnzahlKrankheitenProSeite =
    computed(() => Math.max(this.krankheitenLeft().length, this.krankheitenRight().length));

  constructor() {
    effect(onCleanup => {
      const group = this.impfentscheideFormGroup();
      this.#errors.set(group.errors);
      const subscription = group.statusChanges.subscribe(() => this.#errors.set(group.errors));
      onCleanup(() => subscription.unsubscribe());
    });
  }

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
    return krankheitFormGroup.controls[attribute];
  }

  statusForKrankheit(krankheit: Krankheit): 'default' | 'impfschutzGenuegend' | 'impfempfehlung' | 'impfentscheid' {
    if (this.controlForKrankheit(krankheit, 'impfschutzGegeben')?.value) {
      return 'impfempfehlung';
    }
    return 'default';
  }
}


