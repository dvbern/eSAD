import {Location} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {BackendEntityId, isISODateString} from '@dvb/data-access-types';
import {
  BenutzerStore,
  NotizenListFilterForm,
  NotizFilterForm,
  NotizJsonEnhanced,
  NotizListComponent,
  PageComponent,
  Path
} from '@dvb/feat-shared';
import {
  PageHeaderAddButtonComponent,
  PageHeaderButtonsComponent,
  PageHeaderListSummaryComponent,
  SummaryListItem,
} from '@dvb/ui-components';
import {FilterSessionStorage, TableModel} from '@dvb/util-angular';
import {ensurePropsFlat, hasNotizEditBerechtigung, toValidEnumList} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {DossierStore} from '../../dossier.store';
import {KinderNotizStore} from '../kinder-notiz/kinder-notiz.store';
import {NotizStatus} from '@dvb/data-access-backend';

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
    PageHeaderListSummaryComponent,
    PageHeaderButtonsComponent,
  ],
})
export class KinderNotizenListePageComponent extends FilterSessionStorage<NotizFilterForm> implements OnInit {
  readonly kinderNotizStore = inject(KinderNotizStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly dossierStore = inject(DossierStore);
  readonly location = inject(Location);
  readonly #router = inject(Router);
  readonly #route = inject(ActivatedRoute);
  readonly #transloco = inject(TranslocoService);
  protected readonly Path = Path;

  readonly SESSIONSTORAGE_KEY = 'notizenFilter';

  itemTable = new TableModel<NotizJsonEnhanced>(
    { rawListSignal: this.kinderNotizStore.entities },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );
  readonly tableFilterForm = new NotizenListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  summaryItems = computed((): SummaryListItem[] => [
    {
      name: this.#transloco.translate('kind.dossier.notizen', {}),
      totalCount: this.itemTable.rawListSize(),
      filteredCount: this.itemTable.filteredListSize(),
    },
  ]);

  canEditNotiz = computed(
    () => this.benutzerStore.createHasRequiredRoleSig(hasNotizEditBerechtigung()) && this.dossierStore.isKindAktiv(),
  );

  async goToNotiz(notizId: BackendEntityId) {
    await this.#router.navigate([Path.RELATIVE_ONE_UP, notizId], { relativeTo: this.#route });
  }

  async removeNotiz(notizId: BackendEntityId) {
    this.kinderNotizStore.removeNotiz({
      notizId: notizId
    });
  }

  ngOnInit() {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }
  }

  mapSessionStorageStringToFilter(sessionStorageFilter: string): NotizFilterForm | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as NotizFilterForm;

      // Reconstruct the filter with proper data types and validation
      return ensurePropsFlat<NotizFilterForm>({
        erstelltAm: isISODateString(parsed.erstelltAm) ? parsed.erstelltAm : null,
        titel: parsed.titel ?? '',
        label: Array.isArray(parsed.label) ? parsed.label : [],
        status: toValidEnumList(NotizStatus, parsed.status),
        verantwortlichePerson: Array.isArray(parsed.verantwortlichePerson) ? parsed.verantwortlichePerson : [],
        kindFullName: parsed.kindFullName,
        hasAnhang: parsed.hasAnhang,
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }
}
