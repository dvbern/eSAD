import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { BenutzerOptionJson, BriefvorlageJson, BriefvorlageType } from '@dvb/data-access-backend';
import {BenutzerOptionsStore, BriefvorlagenStore} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {ConfirmService} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {TenantSettingsStore} from '@dvb/feat-shared';
import {
  CardComponent,
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  NgSelectDirective,
  PageHeaderAddButtonComponent,
  PageHeaderButtonsComponent,
  PageHeaderListSummaryComponent,
  SortableComponent,
  SummaryListItem,
  TableComponent,
  TableFilterComponent,
  TableLinkComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import { AppNotificationStore, TableModel } from '@dvb/util-angular';
import { hasBriefvorlageEditBerechtigung } from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { NgbDropdownItem } from '@ng-bootstrap/ng-bootstrap';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { BriefvorlagenListFilterForm } from './briefvorlagen-list-filter.form';

@Component({
  selector: 'feat-briefvorlagen-list.page',
  imports: [
    CommonModule,
    CardComponent,
    ColWidthComponent,
    CollapsibleMenuComponent,
    EmptyTableMessageComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    NgbDropdownItem,
    PageComponent,
    PageHeaderAddButtonComponent,
    PageHeaderButtonsComponent,
    PageHeaderListSummaryComponent,
    ReactiveFormsModule,
    SortableComponent,
    TableComponent,
    TableFilterComponent,
    TableLinkComponent,
    TextInputDirective,
    TranslocoPipe,
  ],
  templateUrl: './briefvorlagen-list-page.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BriefvorlagenListPageComponent {
  readonly store = inject(BriefvorlagenStore);
  readonly #benutzerStore = inject(BenutzerStore);
  readonly #transloco = inject(TranslocoService);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly #confirmService = inject(ConfirmService);
  readonly #tenantSettingsStore = inject(TenantSettingsStore);
  readonly #appNotificationStore = inject(AppNotificationStore);

  protected readonly Path = Path;
  readonly briefvorlageOptions = Object.values(BriefvorlageType);
  readonly spracheOptions = this.#tenantSettingsStore.supportedLanguages();

  editBriefvorlagenBenutzerOptions = computed(() => {
    const adminUsers = this.benutzerOptionsStore.adminuserOptions();
    const superUsers = this.benutzerOptionsStore.supperuserOptions();
    const supportUsers = this.benutzerOptionsStore.supportOptions();

    const allUsers = [...adminUsers, ...superUsers, ...supportUsers];
    return [...new Map(allUsers.map(user => [user.id, user])).values()];
  });

  itemTable = new TableModel<BriefvorlageJson>(
    { rawListSignal: this.store.entities },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );
  readonly tableFilterForm = new BriefvorlagenListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  canEditBriefvorlagen = this.#benutzerStore.createHasRequiredRoleSig(hasBriefvorlageEditBerechtigung());
  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.#transloco.translate('briefvorlagen.list.summary.briefvorlagen'),
        totalCount: this.itemTable.rawListSize(),
        filteredCount: this.itemTable.filteredListSize(),
      },
    ];
    return items;
  });

  compareBenutzerFn(item: BenutzerOptionJson, selected: BenutzerOptionJson): boolean {
    return item.id === selected.id;
  }

  public removeItem(item: BriefvorlageJson): void {
    this.#confirmService
      .confirm({
        question: this.#transloco.translate('briefvorlagen.list.delete.question'),
        consequence: this.#transloco.translate('briefvorlagen.list.delete.consequence'),
        decision: this.#transloco.translate('briefvorlagen.list.delete.decision'),
      })
      .subscribe(() => {
        this.store.removeBriefvorlagen(item.id);
      });
  }

  public generateTest(item: BriefvorlageJson): void {
    this.store.downloadTestBrief({
      briefvorlageId: item.id,
      onSuccess: () => {
        this.#appNotificationStore.addSuccessNotification(
          this.#transloco.translate('kind.dossier.briefgenerieren.success'),
        );
      },
    });
  }
}
