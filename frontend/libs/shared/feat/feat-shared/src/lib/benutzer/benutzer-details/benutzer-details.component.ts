import {ChangeDetectionStrategy, Component, computed, effect, inject, input, output, untracked} from "@angular/core";
import {
  FormButtonsComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormLayoutComponent, InputWithIconComponent, NgSelectDirective, TextInputDirective
} from "@dvb/ui-components";
import {TranslatedEnumViewValuePipe} from '../../translated-strings/view/translated-enum-view-value.pipe';
import {TranslocoPipe} from "@jsverse/transloco";
import {NonNullableFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgOptionComponent, NgSelectComponent} from "@ng-select/ng-select";
import {DirtyMarker} from "@dvb/util-angular";
import {BenutzerDetailsForm} from "./benutzer-details.form";
import {TenantSettingsStore} from "../../tenant/tenant-settings.store";
import {hasOrganisationVerwaltungSupportBerechtigung, isNotNullish, isNullish, Nullish} from "@dvb/util-shared";
import {BenutzerListStore} from "../../admin-shared/benutzer-list.store";
import {BenutzerStore} from "../benutzer/benutzer.store";
import {
  BenutzerSaveJson,
  EntityAktivStatus,
  FachRolle,
  OrganisationJson,
  OrganisationRollenJson
} from "@dvb/data-access-backend";
import {Path} from "../../core/path";
import {OrganisationStore} from "../../admin-shared/organisation.store";

@Component({
  selector: 'feat-shared-benutzer-details',
  imports: [
    TranslocoPipe,
    FormLayoutComponent,
    ReactiveFormsModule,
    FormButtonsComponent,
    FormErrorMessageComponent,
    FormControlComponent,
    TextInputDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TranslatedEnumViewValuePipe,
    InputWithIconComponent,
  ],
  standalone: true,
  templateUrl: './benutzer-details.component.html',
  styleUrl: './benutzer-details.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BenutzerDetailsComponent implements DirtyMarker {
  readonly organisation = input.required<OrganisationJson | Nullish>();
  readonly canEditBenutzer = input.required<boolean>();
  readonly alwaysCreateNewOrganisation = input.required<boolean>();

  readonly saveAction = output();

  readonly benutzerListStore = inject(BenutzerListStore);
  readonly organisationStore = inject(OrganisationStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  // TODO ESC-1180: listen trennen zwischen SystemRollen und OrganisationRollen
  readonly fachrollen = this.tenantSettingsStore.settings()
    ?.fachrollen
    .filter(rolle => rolle !== FachRolle.SYSTEM_ADMIN && rolle !== FachRolle.SUPPORT) ?? [];
  benutzerAktivStati = Object.values(EntityAktivStatus);

  // Der Support kann beim erstellen eines Benutzer über Benutzer & Rechte eine Organisation auswählen
  possibleOrganisationsRollen = computed(() =>
    this.organisationStore.entities()
      .map(organisation => ({organisation: organisation, fachRollen: []} as OrganisationRollenJson))
  );

  showOrganisation = computed(() => isNullish(this.organisation())
    && this.benutzerListStore.selectedIsNew()
    && !this.tenantSettingsStore.zentraleOrganisationEnabled()
    && !this.alwaysCreateNewOrganisation()
    && this.benutzerStore.createHasRequiredRoleSig(hasOrganisationVerwaltungSupportBerechtigung())
  );

  protected readonly Path = Path;

  public form = new BenutzerDetailsForm({
    fb: inject(NonNullableFormBuilder),
    tenantSettingStore: this.tenantSettingsStore
  });

  constructor() {
    effect(() => {
      const selectedEntity = this.benutzerListStore.selectedEntity();
      const mode = this.benutzerListStore.detailMode();

      if (isNullish(selectedEntity)) {
        this.form.group.controls.email.enable();

        // Organisation kann nur beim erstellen gesetzt werden.
        const organisationRolle: OrganisationRollenJson = {
          organisation: this.organisation() ?? undefined,
          fachRollen: []
        };
        this.form.group.controls.organisationRollen.reset([organisationRolle]);

        if (this.organisationStore.selectedIsNew() && this.alwaysCreateNewOrganisation()) {
          this.form.group.controls.fachRollen.reset([FachRolle.SCHULAERZTIN]);
        }

        return;
      }
      untracked(() => {
        this.form.resetForm(selectedEntity, mode);
        this.form.group.controls.email.disable();
      });
    });
  }

  isDirty(): boolean {
    return this.form.isDirty() || this.benutzerListStore.loaderSave.isLoading();
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload: BenutzerSaveJson = this.form.valueOnValid();
      if (this.benutzerListStore.selectedIsNew()) {
        this.benutzerListStore.createBenutzer(
          {
            saveJson: payload,
            onSuccess: () => this.resetAndSave(),
          },
        );
      } else {
        this.benutzerListStore.updateBenutzer(
          {
            saveJson: payload,
            onSuccess: () => this.resetAndSave(),
          },
        );
      }
    }
  }

  isFachrolleDisabled(fachrolle: FachRolle): boolean {
    return fachrolle === FachRolle.SCHULAERZTIN
      && (
        (this.organisationStore.selectedIsNew() && this.alwaysCreateNewOrganisation())
        || (isNotNullish(this.organisation()) && this.organisation()?.verantwortlicherArztFullname === this.benutzerListStore.selectedEntity()?.data.fullName)
      );
  }

  private resetAndSave(): void {
    this.form.reset();
    this.saveAction.emit();
  }

  getOrganisationName(value: OrganisationRollenJson[]): string {
    if (value && value.length > 0 && isNotNullish(value[0].organisation)) {
      return value[0].organisation.verantwortlicherArztFullname;
    }
    return '';
  }
}
