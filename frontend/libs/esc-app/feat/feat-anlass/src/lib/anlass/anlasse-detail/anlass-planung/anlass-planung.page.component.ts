import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  linkedSignal,
  OnDestroy,
} from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AnlassStatus } from '@dvb/data-access-backend';
import { BenutzerStore, ConfirmService, globalConstants, PageComponent, Path } from '@dvb/feat-shared';
import { ButtonComponent, NgSelectDirective, PageHeaderButtonsComponent } from '@dvb/ui-components';
import { AppNotificationStore } from '@dvb/util-angular';
import { fromISODateString, hasExportAnlassPlanungBerechtigung, isNotNullish, requireValue } from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import {interval, startWith, Unsubscribable} from 'rxjs';
import { AnlassStore } from '../../anlass.store';
import { AnlassJsonEnhanced, anlassTitle } from '../../anlass.util';
import { AnlassKinderStore } from '../anlass-kinder.store';
import { DatenComponent } from './daten/daten.component';
import { EinladungenListeComponent } from './einladungen-liste/einladungen-liste.component';

@Component({
  selector: 'feat-anlass-detail-page',
  templateUrl: './anlass-planung.page.component.html',
  styleUrl: './anlass-planung.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    DatenComponent,
    EinladungenListeComponent,
    PageComponent,
    TranslocoPipe,
    PageHeaderButtonsComponent,
    ButtonComponent,
    ReactiveFormsModule,
    FormsModule,
    DatenComponent,
    NgSelectComponent,
    NgOptionComponent,
    NgSelectDirective,
  ],
})
export class AnlassPlanungPageComponent implements OnDestroy, AfterViewInit {
  readonly store = inject(AnlassStore);
  readonly anlassKinderStore = inject(AnlassKinderStore);
  readonly transloco = inject(TranslocoService);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly appNotificationStore = inject(AppNotificationStore);
  readonly confirmService = inject(ConfirmService);
  // start polling for 'naechste Station' changes of Eingeladene Kinder

  #pollingSubscription: Unsubscribable | undefined = undefined;
  protected readonly Path = Path;

  readonly anlassStatusList = Object.values(AnlassStatus);
  protected readonly fromISODateString = fromISODateString;
  canDownloadExport = this.benutzerStore.createHasRequiredRoleSig(hasExportAnlassPlanungBerechtigung());
  pageTitle = computed(() => {
    const currentAnlassData = this.store.selectedEntity()?.data;
    return currentAnlassData
      ? anlassTitle(currentAnlassData, this.transloco)
      : this.transloco.translate('anlass.detail.page.title.edit');
  });

  anlassStatus = linkedSignal(() => this.store.selectedEntity()?.data?.anlassStatus);

  ngAfterViewInit(): void {
    this.#pollingSubscription = this.anlassKinderStore.pollEinladungen(
      interval(globalConstants.pollingIntervalMilliSeconds).pipe(
        // kein initialer Poll, damit sich der Aufruf nicht mit dem initialen Laden der Seite
        // überlagert. Siehe ESC-742
      ));
  }

  anzahlAusgewaehlteKinder(): number {
    return this.anlassKinderStore.selectedForUntersuchung().length;
  }

  gotoBearbeitung(): void {
    this.#router.navigate([Path.RELATIVE_ONE_UP, Path.ANLASS_BEARBEITUNG], { relativeTo: this.#activatedRoute });
  }

  public gotoUntersuchung(): void {
    const einladungIds = this.anlassKinderStore
      .selectedForUntersuchung()
      .map(einladungKind => einladungKind.id)
      .join(',');
    const firstEinladung = this.anlassKinderStore
      .selectedForUntersuchung()
      .sort((a, b) => a.kind.data.name.localeCompare(b.kind.data.name))[0].id;
    this.#router.navigate(
      [Path.RELATIVE_ONE_UP, Path.ANLASS_UNTERSUCHUNG, Path.ANLASS_UNTERSUCHUNG_EINLADUNG, firstEinladung],
      { relativeTo: this.#activatedRoute, queryParams: { einladungIds: einladungIds } },
    );
  }

  onStatusChange(status: AnlassStatus): void {
    const anlass = this.store.selectedEntity();
    if (isNotNullish(anlass)) {
      if (isNotNullish(status)) {
        if (status === AnlassStatus.ABGESCHLOSSEN) {
          this.confirmService
            .makeDecision({
              question: this.transloco.translate('anlass.status.abschluss.question'),
              consequence: this.transloco.translate('anlass.status.abschluss.consequence'),
              decision: this.transloco.translate('anlass.status.abschluss.decision'),
            })
            .subscribe(value => {
              if (value) {
                this.saveStatusChange(status, anlass);
              } else {
                /**
               * Die Select Komponente behält im Signal den vorherigen Wert aber es wird im GUI Abgeschlossen
               * angezeigt.
               * Damit ein Wechsel getriggert wird, setze ich den Wert zuerst auf undefined und danach wieder zurück
               * auf den effektiven Wert.
               //  */
                this.anlassStatus.set(undefined);
                setTimeout(() => {
                  this.anlassStatus.set(anlass.data.anlassStatus);
                }, 0);
              }
            });
        } else {
          this.saveStatusChange(status, anlass);
        }
      }
    }
  }

  saveStatusChange(status: AnlassStatus, anlass: AnlassJsonEnhanced): void {
    this.store.changeAnlassStatus({
      anlassId: anlass.id,
      status: status,
      onSuccess: () => {
        this.appNotificationStore.addNotification({
          alertType: 'success',
          id: crypto.randomUUID(),
          summary: this.transloco.translate('anlass.status.change.success'),
          autohide: true,
        });
      },
    });
  }

  public downloadPrint(): void {
    const anlassId = requireValue(this.store.selectedEntity()?.id);
    this.store.downloadAnlassExportPrint({ anlassId: anlassId });
  }

  protected readonly event = event;

  public ngOnDestroy() {
    this.#pollingSubscription?.unsubscribe();
  }
}
