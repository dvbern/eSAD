import {inject, Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {withSelectedEntity} from '@dvb/util-angular';
import {patchState, signalStore} from '@ngrx/signals';
import {setAllEntities, withEntities} from '@ngrx/signals/entities';

export interface MenuItemInfo {
  label: string;
  id: string;
}

@Injectable()
export class PlaygroundNavUntersuchungStore extends signalStore(
  {protectedState: false},
  // withDevtools('playgroundNavUntersuchung'),
  withEntities<MenuItemInfo>(),
  withSelectedEntity<MenuItemInfo>(),
) {
  readonly router = inject(Router);

  init(): void {
    patchState(this, setAllEntities([
      {id: 'daten', label: 'Daten'},
      {id: 'impfungen', label: 'Impfungen'},
      {id: 'sehen', label: 'Sehen'},
      {id: 'hoeren', label: 'Hören'},
      {id: 'groesse', label: 'Grösse & Gewicht'},
    ]));
  }

}
