import {ChangeDetectionStrategy, Component, effect, inject, model} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ArtAbmeldung, EinladungJson, GrundAbmeldung} from '@dvb/data-access-backend';
import {ModalDialogComponent} from '@dvb/feat-shared';
import {
  FormControlComponent,
  FormLayoutComponent,
  NgSelectDirective,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TextInputDirective,
} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {EinladungAbmeldenForm} from './einladung-abmelden.form';

@Component({
    selector: 'feat-anlass-planung-einladungen-abmelden-modal',
    templateUrl: './einladung-abmelden.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        FormsModule,
        TranslocoPipe,
        TextInputDirective,
        NgOptionComponent,
        NgSelectComponent,
        NgSelectDirective,
        ReactiveFormsModule,
        FormControlComponent,
        FormLayoutComponent,
        TextareaDirective,
        ModalDialogComponent,
        SaveCancelButtonsComponent,
    ]
})
export class EinladungAbmeldenComponent {
  readonly activeModal = inject(NgbActiveModal);

  protected readonly abmeldungsgruende = Object.values(GrundAbmeldung);
  protected readonly abmeldungsarten = Object.values(ArtAbmeldung);
  protected readonly DetailMode = DetailMode;

  form = new EinladungAbmeldenForm(inject(NonNullableFormBuilder));

  einladungData = model<EinladungJson>();

  constructor() {
    effect(() => {
      const data = this.einladungData();
      this.form.resetForm(data);
    });
  }

  abbrechen(): void {
    this.activeModal.dismiss('cancel');
  }

  async speichern(): Promise<void> {
    if (await this.form.validate()) {
      this.activeModal.close(this.form.valueOnValid());
    }
  }
}
