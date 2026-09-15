import { computed, inject, Injectable } from '@angular/core';
import { ClientConfigService, ConfigJson } from '@dvb/data-access-backend';
import { AbstractSignalStore } from '@dvb/util-angular';
import { requireValue } from '@dvb/util-shared';
import * as Sentry from '@sentry/browser';
import { firstValueFrom } from 'rxjs';

type DeploymentState = {
  config: ConfigJson | undefined;
};

const initialState: DeploymentState = {
  config: undefined,
};

@Injectable({ providedIn: 'root' })
export class DeploymentStore extends AbstractSignalStore<DeploymentState> {
  /**
   * @param testInit - for testing purposes only
   */
  constructor() {
    const testInit = inject<DeploymentState>('nothing' as never, { optional: true });

    super(testInit ?? initialState);
  }

  readonly clientConfigService = inject(ClientConfigService);

  async init(): Promise<void> {
    const config = await firstValueFrom(this.clientConfigService.config$());
    Sentry.setTag('environment', config.clientDeploymentConfigJson.environment);
    this.patchState(state => {
      state.config = config;
    });
  }

  // since the application cannot work without clientConfig, we can always assume requireConfig()
  config = computed(() => {
    return requireValue(this.state().config);
  });

  isTimetravelEnabled = computed(() => {
    return requireValue(this.config()).featureConfigJson.timetravelEnabled;
  });

  archiveBulkDownloadMaxFiles = computed(() => {
    return requireValue(this.config()).featureConfigJson.archiveBulkDownloadMaxFiles;
  });

  environment = computed(() => this.config().clientDeploymentConfigJson.environment);

  publicUrl = computed(() => this.config().clientDeploymentConfigJson.publicUrl);

  version = computed(() => this.config().clientDeploymentConfigJson.version);
}
