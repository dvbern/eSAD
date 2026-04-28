import {computed, inject, Injectable} from '@angular/core';
import {LabelJson, LabelSaveJson, LabelsService, LabelTyp} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {AppErrorHandlerService, Loader, withSelectedEntity} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withComputed, withMethods} from '@ngrx/signals';
import {
  addEntity,
  removeAllEntities,
  removeEntity,
  setAllEntities,
  setEntity,
  withEntities,
} from '@ngrx/signals/entities';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {exhaustMap, pipe, switchMap, tap} from 'rxjs';
import {TranslatedStringPipe} from '../translated-strings/view/translated-string.pipe';

const IMPFAUSWEIS_AUTO_LABEL_ID: BackendEntityId = '350f2746-7db4-4dfa-ae28-1b3b5812434a';
const FRAGEBOGEN_AUTO_LABEL_ID: BackendEntityId = 'f8514824-9694-408e-bdb6-d72e376d7833';
const DISPENSATIONSGESUCH_AUTO_LABEL_ID: BackendEntityId = '345d4d49-ef8d-4236-94dc-3e35f4bdf76c';
const ANMELDUNG_BESTAETIGUNG_AUTO_LABEL_ID: BackendEntityId = 'e84f6525-0f70-4440-bb62-11fd25fea5b1';
const IMPFANMELDUNG_AUTO_LABEL_ID: BackendEntityId = '56986f77-1447-4d41-ba3a-1f792cdeb335';

@Injectable({ providedIn: 'root' })
export class LabelsStore extends signalStore(
  { protectedState: false },
  // withDevtools('label'),
  withEntities<LabelJson>(),
  withMethods((store) => ({
    impfausweisAutoLabel: () => {
      return store.entities().find(entity => entity.id === IMPFAUSWEIS_AUTO_LABEL_ID);
    },
    fragebogenAutoLabel: () => {
      return store.entities().find(entity => entity.id === FRAGEBOGEN_AUTO_LABEL_ID);
    },
    anmeldungBestaetigungAutoLabel: () => {
      return store.entities().find(entity => entity.id === ANMELDUNG_BESTAETIGUNG_AUTO_LABEL_ID);
    },
    impfanmeldungAutoLabel: () => {
      return store.entities().find(entity => entity.id === IMPFANMELDUNG_AUTO_LABEL_ID);
    },
    dispensationsgesuchAutoLabel: () => {
      return store.entities().find(entity => entity.id === DISPENSATIONSGESUCH_AUTO_LABEL_ID);
    },
  })),
  withSelectedEntity<LabelJson>(),
  withComputed((store, translatedStringPipe = inject(TranslatedStringPipe)) => ({
    notizenLabelsSorted: computed(() => {
      return [...store.entities()]
        .filter(each => each.data.labelTyp === LabelTyp.NOTIZEN)
        .sort((a: LabelJson, b: LabelJson) => translatedStringPipe.transform(a.data.name)
          .localeCompare(translatedStringPipe.transform(b.data.name)));
    }),
    notizLabelsEnabledSorted: computed(() => {
      return [...store.entities()]
        .filter(each => each.data.labelTyp === LabelTyp.NOTIZEN && each.data.aktiv)
        .sort((a: LabelJson, b: LabelJson) => translatedStringPipe.transform(a.data.name)
          .localeCompare(translatedStringPipe.transform(b.data.name)));
    }),
    aufgabenLabelsSorted: computed(() => {
      return [...store.entities()]
        .filter(each => each.data.labelTyp === LabelTyp.AUFGABEN)
        .sort((a: LabelJson, b: LabelJson) => translatedStringPipe.transform(a.data.name)
          .localeCompare(translatedStringPipe.transform(b.data.name)));
    }),
    aufgabenLabelsEnabledSorted: computed(() => {
      return [...store.entities()]
        .filter(each => each.data.labelTyp === LabelTyp.AUFGABEN && each.data.aktiv)
        .sort((a: LabelJson, b: LabelJson) => translatedStringPipe.transform(a.data.name)
          .localeCompare(translatedStringPipe.transform(b.data.name)));
    }),
  })),
) {
  readonly #labelsService = inject(LabelsService);
  readonly #appErrorHandlerService = inject(AppErrorHandlerService);
  readonly loader = new Loader();
  readonly deleteLoader = new Loader();
  readonly saveLoader = new Loader();

  loadLabelsList = rxMethod<void>(
    pipe(
      tap(() => patchState(this, removeAllEntities())),
      switchMap(() => this.loader.tapLoading$(this.#labelsService.findAllLabels$(),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: labelList => patchState(this, setAllEntities(labelList.labelList)),
        error: console.error,
      }),
    ),
  );

  filterLabelsByTypeList = rxMethod<LabelTyp>(
    pipe(
      switchMap(labelTyp => this.loader.tapLoading$(
        this.#labelsService.filterLabels$({ type: labelTyp }),
        error => this.#appErrorHandlerService.handleError(error))),
      tapResponse({
        next: labelList => patchState(this, setAllEntities(labelList.labelList)),
        error: console.error,
      }),
    ),
  );

  createLabel = rxMethod<{ newLabel: LabelSaveJson, onSuccess: () => void; }>(
    pipe(
      exhaustMap(({ newLabel, onSuccess }) =>
        this.saveLoader.tapLoading$(
          this.#labelsService.createLabel$({ labelSaveJson: newLabel }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, addEntity(result));
              onSuccess();
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  updateLabel = rxMethod<{ labelToUpdate: LabelSaveJson, onSuccess: () => void }>(
    pipe(
      exhaustMap(
        ({ labelToUpdate, onSuccess }) => this.saveLoader.tapLoading$(
          this.#labelsService.updateLabel$({
            labelId: requireValue(this.selectedEntity()?.id),
            labelSaveJson: labelToUpdate,
          }),
          error => this.#appErrorHandlerService.handleError(error),
        ).pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(result));
              onSuccess();
            },
            error: console.error,
          }),
        ),
      ),
    ),
  );

  removeLabel = rxMethod<BackendEntityId>(
    pipe(
      exhaustMap(idToDelete =>
        this.deleteLoader
          .tapLoading$(
            this.#labelsService.removeLabel$({
              labelId: requireValue(idToDelete),
            }),
            error => this.#appErrorHandlerService.handleError(error),
          )
          .pipe(
            tapResponse({
              next: () => patchState(this, removeEntity(idToDelete)),
              error: console.error,
            }),
          ),
      ),
    ),
  );

  toggleActiveOnLabel = rxMethod<LabelJson>(
    pipe(
      exhaustMap((labelJson) => this.saveLoader
        .tapLoading$(
          this.#labelsService.updateLabel$({
            labelId: labelJson.id,
            labelSaveJson: {...labelJson.data, aktiv: !labelJson.data.aktiv},
          }), error => this.#appErrorHandlerService.handleError(error))
        .pipe(
          tapResponse({
            next: result => {
              patchState(this, setEntity(result));
            },
            error: console.error,
          }),
        ),
      )),
  );
}
