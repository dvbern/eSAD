import {computed, inject, Injectable} from '@angular/core';
import {AnlassStatus, KindanlaesseService, KindAnlassSummaryJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {isNullish} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withComputed} from '@ngrx/signals';
import {setAllEntities, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, switchMap} from 'rxjs';

@Injectable()
export class KindAnlaesseStore extends signalStore(
  {protectedState: false},
  // withDevtools('notizen'),
  withEntities<KindAnlassSummaryJson>(),
  withSelectedEntity<KindAnlassSummaryJson>(),
  withComputed(({entities}) => ({
    isKindInAnyAbgeschlossenAnlass: computed((): boolean => {
      if (isNullish(withSelectedEntity())) {
        return false;
      }
      return entities().filter(anlass => anlass.anlassStatus === AnlassStatus.ABGESCHLOSSEN).length > 0;
    }),
  })),
) {
  readonly kindanlaesseService = inject(KindanlaesseService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);

  readonly loader = new Loader();
  readonly saveLoader = new Loader();

  loadAnlaesseForKind = rxMethod<BackendEntityId>(
    pipe(
      switchMap((kindId) => this.loader.tapLoading$(
        this.kindanlaesseService.findAnlaesseForKind$({
          kindId: kindId,
        }),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: listJson => patchState(this, setAllEntities(listJson.anlassList
          .sort((a, b) => b.anlassDatum.localeCompare(a.anlassDatum)))),
        error: console.error,
      }),
    ),
  );

}
