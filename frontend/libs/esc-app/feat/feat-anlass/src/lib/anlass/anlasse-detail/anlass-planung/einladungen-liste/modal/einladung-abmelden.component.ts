import {ChangeDetectionStrategy, Component, effect, inject, model, output} from '@angular/core';
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
import {EinladungAbmeldenForm, EinladungAbmeldenJson} from './einladung-abmelden.form';

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
  ],
  standalone: true
})
export class EinladungAbmeldenComponent {
  readonly activeModal = inject(NgbActiveModal);

  protected readonly abmeldungsgruende = Object.values(GrundAbmeldung);
  protected readonly abmeldungsarten = Object.values(ArtAbmeldung);
  protected readonly DetailMode = DetailMode;

  form = new EinladungAbmeldenForm(inject(NonNullableFormBuilder));

  einladungData = model<EinladungJson>();

  saveForm = output<EinladungAbmeldenJson>();

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
    const einladung = this.einladungData();

    if (await this.form.validate() && einladung) {
      this.saveForm.emit(this.form.valueOnValid());
    }
  }
}
