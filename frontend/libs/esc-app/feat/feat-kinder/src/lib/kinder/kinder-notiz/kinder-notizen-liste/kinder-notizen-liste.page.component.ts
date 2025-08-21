import {Location} from '@angular/common';
import {ChangeDetectionStrategy, Component, DestroyRef, inject} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {BackendEntityId} from '@dvb/data-access-types';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {NotizenListFilterForm, NotizJsonEnhanced, NotizListComponent} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {PageHeaderAddButtonComponent} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {hasNotizEditBerechtigung, requireValue} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {DossierStore} from '../../dossier.store';
import {KinderNotizStore} from '../kinder-notiz/kinder-notiz.store';

@Component({
  selector: 'feat-kinder-notizen.page',
  templateUrl: './kinder-notizen-liste.page.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    TranslocoPipe,
    PageHeaderAddButtonComponent,
    NotizListComponent,
  ],
})
export class KinderNotizenListePageComponent {
  readonly kinderNotizStore = inject(KinderNotizStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly dossierStore = inject(DossierStore);
  readonly location = inject(Location);
  notizenTable = new TableModel<NotizJsonEnhanced>({rawListSignal: this.kinderNotizStore.entities},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});
  readonly tableFilterForm = new NotizenListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.notizenTable,
  });
  canEditNotiz = this.benutzerStore.createHasRequiredRoleSig(hasNotizEditBerechtigung());
  protected readonly Path = Path;
  readonly #router = inject(Router);
  readonly #route = inject(ActivatedRoute);

  async goToNotiz(notizId: BackendEntityId) {
    await this.#router.navigate([Path.RELATIVE_ONE_UP, notizId], {relativeTo: this.#route});
  }

  async removeNotiz(notizId: BackendEntityId) {
    this.kinderNotizStore.removeNotiz({
      kindId: requireValue(this.dossierStore.selectedKind()?.id),
      notizId: notizId,
    });
  }
}
