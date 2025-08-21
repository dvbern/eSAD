import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, effect, inject, untracked} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {DetailMode, DirtyMarker} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {StammdatenForm} from './stammdaten.form';
import {StammdatenStore} from './stammdaten.store';

@Component({
    selector: 'feat-stammdaten',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [CommonModule, ReactiveFormsModule, TranslocoPipe],
    templateUrl: './stammdaten.component.html',
    styles: ':host { display: block; }'
})
export class StammdatenComponent implements DirtyMarker {
  readonly store = inject(StammdatenStore);
  readonly form = new StammdatenForm();

  constructor() {
    effect(() => {
      const stammdaten = this.store.stammdaten();
      const errors = this.store.backendErrors();

      untracked(() => {
        this.form.reset(stammdaten, DetailMode.EDIT, errors);
      });
    });
  }

  isDirty(): boolean {
    const result = this.form.isDirty() || this.store.saveLoader.isLoading();
    return result;
  }

  demo(): void {
    window.alert('Demo');
  }

  async submit(): Promise<void> {
    if (await this.form.validate()) {
      this.store.updateStammdaten(this.form.valueOnValid());
    }
  }
}
