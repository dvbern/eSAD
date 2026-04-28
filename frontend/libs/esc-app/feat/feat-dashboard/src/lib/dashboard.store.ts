import {inject, Injectable} from '@angular/core';
import {
  AktivitaetService,
  AnlassAktivitaetHistoryJson,
  AnlassService,
  AufgabeService, KindAktivitaetHistoryJson,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AufgabeJsonEnhanced, enhanceAufgabe} from '@dvb/feat-shared';
import {AnlassJsonEnhanced, enhanceAnlass} from '@dvb/feat-anlass';
import {AppErrorHandlerService, Loader} from '@dvb/util-angular';
import {TranslocoService} from '@jsverse/transloco';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {forkJoin, pipe, switchMap, tap} from 'rxjs';

type DashboardState = {
  anlassList: AnlassJsonEnhanced[];
  totalRecords: number;
  aufgabenList: AufgabeJsonEnhanced[];
    anlassAktivitaetHistory: AnlassAktivitaetHistoryJson[]; // shows the last openend anlaesse and kinder
    kinderAktivitaetHistory: KindAktivitaetHistoryJson[];
};

const initialState: DashboardState = {
  anlassList: [],
  totalRecords: 0,
  aufgabenList: [],
  anlassAktivitaetHistory: [],
  kinderAktivitaetHistory: [],
};

@Injectable()
export class DashboardStore extends signalStore(
        {protectedState: false},
  // withDevtools('dashboard'),
  withState(initialState),
) {
  readonly #anlassService = inject(AnlassService);
  readonly #aktivitaetService = inject(AktivitaetService);
  readonly #aufgabenService = inject(AufgabeService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly transloco = inject(TranslocoService);

  readonly loader = new Loader();
  readonly letzteAktivitaetHistoryLoader = new Loader();

  loadAnlassListForUser = rxMethod<BackendEntityId>(
    pipe(
      switchMap(benutzerId => this.loader.tapLoading$(
          this.#anlassService.findAnlaesseForDashboard$({ benutzerID: benutzerId }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: listJson => {
          patchState(this,
            {
                anlassList: listJson.anlassList.map(each => enhanceAnlass(each,
                        this.transloco)),
              totalRecords: listJson.totalRecords,
            });
        },
        error: console.error,
      }),
    ),
  );

  loadAllNichtAbgeschlosseneForCurrentUser = rxMethod<void>(
    pipe(
      switchMap(() => this.loader.tapLoading$(
          this.#aufgabenService.findAllNichtAbgeschlosseneAufgabenForCurrent$(),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: listJson => {
          patchState(this, {
            aufgabenList: listJson.aufgaben.map(each => enhanceAufgabe(each)),
          });
        },
        error: console.error,
      }),
    ),
  );

  loaddAktivitaetHistoryForUser = rxMethod<void>(
    pipe(
      tap(() => patchState(this, {
        anlassAktivitaetHistory: [],
        kinderAktivitaetHistory: [],
      })),
      switchMap(() => this.letzteAktivitaetHistoryLoader.tapLoading$(
          forkJoin({
            kinder: this.#aktivitaetService.findLetzteKinder$(),
            anlaesse: this.#aktivitaetService.findLetzteAnlaesse$(),
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: result => {
          patchState(this,
            {
              anlassAktivitaetHistory: result.anlaesse.anlassAktivitaetList,
              kinderAktivitaetHistory: result.kinder.kindAktivitaetList,
            });
        },
        error: console.error,
      }),
    ),
  );
}
