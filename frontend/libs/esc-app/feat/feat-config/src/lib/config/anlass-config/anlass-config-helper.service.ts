import {computed, inject, Injectable} from '@angular/core';
import {BenutzerStore, DeploymentStore, Path, TenantSettingsStore} from '@dvb/feat-shared';
import {hasConfigEditBerechtigung} from '@dvb/util-shared';
import {AnlassConfigStore} from './anlass-config.store';
import {isValidBackendEntityId} from '@dvb/data-access-types';
import {DetailMode} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {ActivatedRoute, Router} from '@angular/router';
import {AnlassConfigForm} from './anlass-config/anlass-config-detail.form';
import {DeploymentEnvironment} from "@dvb/data-access-backend";

@Injectable({ providedIn: 'root' })
export class AnlassConfigHelperService {
  readonly store = inject(AnlassConfigStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly deploymentStore = inject(DeploymentStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly transloco = inject(TranslocoService);
  readonly #router = inject(Router);

  neverEditable = computed(() => {
    return this.deploymentStore.environment() === DeploymentEnvironment.UAT
      || this.deploymentStore.environment() === DeploymentEnvironment.PROD;
  });

  canEdit = computed(() => {
    const hasRole = this.benutzerStore.createHasRequiredRoleSig(hasConfigEditBerechtigung())();
    return hasRole && !this.neverEditable();
  });

  isDirty(form: AnlassConfigForm): boolean {
    return form.isDirty() || this.store.saveLoader.isLoading();
  }

  async submitIfValid(form: AnlassConfigForm, activatedRoute: ActivatedRoute): Promise<void> {
    if (await form.validate()) {
      const payload = form.valueOnValid();
      if (isValidBackendEntityId(this.store.selectedEntityId())) {
        this.store.updateAnlassConfig({
          anlassConfig: payload,
          onSuccess: () => {
            form.reset();
            this.#router.navigate([Path.RELATIVE_ONE_UP, DetailMode.VIEW], { relativeTo: activatedRoute });
          },
        });
      } else {
        this.store.createAnlassConfig({
          anlassConfig: payload,
          onSuccess: () => {
            form.reset();
            this.#router.navigate([Path.RELATIVE_TWO_UP], { relativeTo: activatedRoute });
          },
        });
      }
    }
  }
}
