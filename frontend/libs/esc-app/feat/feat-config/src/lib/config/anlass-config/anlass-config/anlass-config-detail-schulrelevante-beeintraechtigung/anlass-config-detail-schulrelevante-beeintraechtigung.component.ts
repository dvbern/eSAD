import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, untracked} from "@angular/core";

import {NonNullableFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {
  FormButtonsComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormLayoutComponent, HeadingDirective, NgSelectDirective
} from "@dvb/ui-components";
import {NgOptionComponent, NgSelectComponent} from "@ng-select/ng-select";
import {TranslocoPipe} from "@jsverse/transloco";
import {DirtyMarker} from "@dvb/util-angular";
import {BenutzerStore, Path} from "@dvb/feat-shared";
import {ActivatedRoute} from "@angular/router";
import { AnlassConfigStore } from '../../anlass-config.store';
import {AnlassConfigHelperService} from "../../anlass-config-helper.service";
import {RequirementSetting} from "@dvb/data-access-backend";
import {AnlassConfigForm} from "../anlass-config-detail.form";
import {isNullish} from "@dvb/util-shared";

@Component({
  selector: 'feat-config-detail-schulrelevante-beeintraechtigung',
  imports: [
    ReactiveFormsModule,
    FormButtonsComponent,
    FormControlComponent,
    FormErrorMessageComponent,
    FormLayoutComponent,
    HeadingDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TranslocoPipe
],
  templateUrl: './anlass-config-detail-schulrelevante-beeintraechtigung.component.html',
  styleUrl: './anlass-config-detail-schulrelevante-beeintraechtigung.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnlassConfigDetailSchulrelevanteBeeintraechtigungComponent implements DirtyMarker {
  readonly store = inject(AnlassConfigStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly anlassConfigHelperService = inject(AnlassConfigHelperService);

  readonly requirementSettingOptions = Object.values(RequirementSetting);
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
