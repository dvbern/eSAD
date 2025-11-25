import {inject, Injectable} from '@angular/core';
import {AnlassConfigOptionJson, AnlassConfigService, AnlassTyp} from '@dvb/data-access-backend';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {patchState, signalStore, withComputed, withMethods} from '@ngrx/signals';
import {setAllEntities, withEntities} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, switchMap} from 'rxjs';
import {tapResponse} from '@ngrx/operators';
import {fromISODateString, isNullish, today} from '@dvb/util-shared';
import {ISODateString} from '@dvb/data-access-types';
import {isAfter, isBefore} from 'date-fns';

@Injectable({providedIn: 'root'})
export class AnlassConfigOptionsStore extends signalStore(
  {protectedState: false},
  withEntities<AnlassConfigOptionJson>(),
  withSelectedEntity<AnlassConfigOptionJson>(),
  withComputed(store => ({
    getVisibleAnlassTypen(): AnlassTyp[] {
      return Array.from(new Set(store.entities().map(config => config.anlassTyp)));
    },
  })),
  withMethods(store => ({
    getOptionsForAnlassTyp(typ: AnlassTyp | undefined): AnlassConfigOptionJson[] {
      if (isNullish(typ)) {
        return [];
      }
      return store.entities().filter((el: AnlassConfigOptionJson) => el.anlassTyp === typ);
    },
    getDefaultOptionForAnlassTyp(
      typ: AnlassTyp | undefined,
      anlassDatum: ISODateString | undefined,
    ): AnlassConfigOptionJson | undefined {
      const options = this.getOptionsForAnlassTyp(typ);
      if (options.length <= 0) {
        return undefined;
      }
      // Wenn das Datum noch nicht ausgewählt wurde, nehmen wir heute als Stichtag
      // const stichtag = anlassDatum ?? toISODateTimeString(today());
      const stichtag = fromISODateString(anlassDatum) ?? today();
      return options.find(value => {
        return isAfter(stichtag, value.datumVon) && isBefore(stichtag, value.datumBis);
      });
    },
  })),
) {
  readonly loader = new Loader();

  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly #anlassConfigService = inject(AnlassConfigService);

  loadAnlassConfigOptions = rxMethod<void>(
    pipe(
      switchMap(() =>
        this.loader.tapLoading$(this.#anlassConfigService.findAllAnlassConfigsVisible$(), error =>
          this.#appErrorHandlerService.handleError(error),
        ),
      ),
      tapResponse({
        next: optionList => patchState(this, setAllEntities(optionList.configList)),
        error: console.error,
      }),
    ),
  );
}
