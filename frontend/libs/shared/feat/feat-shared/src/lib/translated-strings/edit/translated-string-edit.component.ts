import {ChangeDetectionStrategy, Component, computed, inject, input} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import { TenantSettingsStore } from '../../tenant/tenant-settings.store';
import {FormLayoutComponent, FormControlComponent, TextInputDirective} from '@dvb/ui-components';
import {Nullish} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
    selector: 'feat-translated-string-edit',
    imports: [
        FormControlComponent,
        TranslocoPipe,
        ReactiveFormsModule,
        TextInputDirective,
        FormLayoutComponent,
    ],
    templateUrl: './translated-string-edit.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TranslatedStringEditComponent {

  readonly tenantSettingsStore = inject(TenantSettingsStore);

  translatedStringGroup = input.required<FormGroup<{
    de: FormControl<string | Nullish>,
    fr: FormControl<string | Nullish>,
  }>>();

  readonly #formcomponent = inject(FormLayoutComponent);
  mode = computed(() => this.#formcomponent.mode());
}
