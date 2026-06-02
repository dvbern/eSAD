import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, untracked} from "@angular/core";

import {NonNullableFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {
  FormButtonsComponent,
  FormErrorMessageComponent,
  FormLayoutComponent, HeadingDirective
} from "@dvb/ui-components";
import {TranslocoPipe} from "@jsverse/transloco";
import {DirtyMarker} from "@dvb/util-angular";
import {
  AnlassConfigRequirementSettingComponent,
  AnlassConfigSelectBooleanComponent,
  BenutzerStore,
  Path
} from "@dvb/feat-shared";
import {ActivatedRoute} from "@angular/router";
import {AnlassConfigHelperService} from "../../anlass-config-helper.service";
import {AnlassConfigForm} from "../anlass-config-detail.form";
import {isNullish} from "@dvb/util-shared";
import { AnlassConfigStore } from "../../anlass-config.store";

@Component({
  selector: 'feat-config-abschluss',
  imports: [
    ReactiveFormsModule,
    FormButtonsComponent,
    FormErrorMessageComponent,
    FormLayoutComponent,
    HeadingDirective,
    TranslocoPipe,
    AnlassConfigRequirementSettingComponent,
    AnlassConfigSelectBooleanComponent
],
  templateUrl: './anlass-config-detail-abschluss.component.html',
  styleUrl: './anlass-config-detail-abschluss.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnlassConfigDetailAbschlussComponent implements DirtyMarker {
  readonly store = inject(AnlassConfigStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly anlassConfigHelperService = inject(AnlassConfigHelperService);
  protected readonly Path = Path;

  public form = new AnlassConfigForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    anlassConfigStore: this.store,
  });

  constructor() {
    effect(() => {
      const selectedEntity = this.store.selectedEntity();
      const mode = this.store.detailMode();
      if (isNullish(selectedEntity)) {
        return;
      }
      this.form.disableFields(this.store.selectedIsNew());
      untracked(() => {
        this.form.reset(selectedEntity.data, mode);
      });
    });
  }

  isDirty(): boolean {
    return this.anlassConfigHelperService.isDirty(this.form);
  }

  async submitIfValid(): Promise<void> {
    this.anlassConfigHelperService.submitIfValid(this.form, this.#activatedRoute);
  }
}
