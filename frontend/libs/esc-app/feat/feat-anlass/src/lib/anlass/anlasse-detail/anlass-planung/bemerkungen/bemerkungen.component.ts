import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {FormControlComponent, FormLayoutComponent, TextareaDirective} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {ensureProps, isNullish} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {AnlassStore} from '../../../anlass.store';
import {BemerkungenSaveJson} from './bemerkungen-save-json';
import {BemerkungenForm} from './bemerkungen.form';

@Component({
    selector: 'feat-anlass-bemerkungen',
    templateUrl: './bemerkungen.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        ReactiveFormsModule,
        ReactiveFormsModule,
        TranslocoPipe,
        FormControlComponent,
        TextareaDirective,
        FormLayoutComponent,
        ReactiveFormsModule,
        TranslocoPipe,
        TextareaDirective,
    ]
})
export class BemerkungenComponent {

  readonly store = inject(AnlassStore);
  readonly benutzerStore = inject(BenutzerStore);

  protected readonly DetailMode = DetailMode;
  protected readonly Path = Path;

  public form = new BemerkungenForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const selectedEntity = this.store.selectedEntity();
      const mode = this.store.detailMode();
      if (isNullish(selectedEntity)) {
        return;
      }
      untracked(() => {
        const payload = ensureProps<BemerkungenSaveJson>({
            arztInfo: selectedEntity.data.arztInfo,
            mpaInfo: selectedEntity.data.mpaInfo,
            weiterePersonInfo: selectedEntity.data.weiterePersonInfo,
          });
        this.form.reset(payload, mode);
      });
    });
  }
}
