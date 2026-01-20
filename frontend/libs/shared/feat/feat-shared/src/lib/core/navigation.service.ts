import {inject, Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {NotizTyp} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {DeploymentStore} from '../deployment/deployment.store';
import {isNullish, Nullish} from '@dvb/util-shared';
import {Path} from './path';

@Injectable({providedIn: 'root'})
export class NavigationService {

  readonly #router = inject(Router);
  readonly deploymentStore = inject(DeploymentStore);

  getLinkToDossier(kindId: BackendEntityId | undefined): string {
    if (isNullish(kindId)) {
      return '';
    }
    return this.deploymentStore.publicUrl() + '/' + Path.KINDER + '/' + kindId;
  }

  navigateToEinladungNotiz(
    anlassId: BackendEntityId | Nullish,
    einladungId: BackendEntityId | Nullish,
    notizTyp: NotizTyp,
  ): void {
    if (isNullish(anlassId) || isNullish(einladungId)) {
      return;
    }
    this.#router.navigate(
      [Path.ANLASS, anlassId, Path.ANLASS_UNTERSUCHUNG_EINLADUNG, einladungId, Path.NOTIZEN, notizTyp],
      {queryParamsHandling: 'merge'});
  }
}
