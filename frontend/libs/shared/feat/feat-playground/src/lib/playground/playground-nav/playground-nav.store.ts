import {computed, inject, Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Path} from '@dvb/feat-shared';
import {Loader, withSelectedEntity} from '@dvb/util-angular';
import {patchState, signalStore, withState} from '@ngrx/signals';
import {withEntities} from '@ngrx/signals/entities';

export interface PlaygroundNavChild {
  id: string;
  name: string;
}

export interface TabInfo {
  label: string;
  id: string;
}

type PlaygroundNavState = {
  availableTabs: TabInfo[] | undefined;
  visibleTabs: TabInfo[];
};

const initialState: PlaygroundNavState = {
  availableTabs: [
    {id: 'kaestner', label: 'Erich Kästner'},
    {id: 'lindgren', label: 'Astrid Lindgren'},
    {id: 'carigiet', label: 'Alois Carigiet'},
    {id: 'janosch', label: 'Janosch'},
    {id: 'ende', label: 'Michael Ende'},
  ],
  visibleTabs: [],
};

@Injectable()
export class PlaygroundNavStore extends signalStore(
  {protectedState: false},
  // withDevtools('playgroundNav'),
  withState(initialState),
  withEntities<PlaygroundNavChild>(),
  withSelectedEntity<PlaygroundNavChild>(),
) {
  readonly loader = new Loader();
  readonly router = inject(Router);

  closeTab(id: string): void {
    patchState(this, {
      visibleTabs: this.visibleTabs().filter(each => each.id !== id),
    });
    if (id === this.selectedEntityId()) {
      // if the selected tab is closed: navigate up one level because otherwise this content is still shown
      this.setSelectedEntityId(null);
      this.router.navigate([Path.PLAYGROUND, 'nav']);
    }
  }

  addTab(tab: TabInfo): void {

    const newTabs = [...this.visibleTabs()];
    newTabs.push(tab);

    patchState(this, {
      visibleTabs: newTabs,
    });

    // navigate to the added tab
    this.router.navigate([Path.PLAYGROUND, 'nav', tab.id]);
  }

  isTabVisible(id: string): boolean {
    return this.visibleTabs().some(open => open.id === id)
  }

  invisibleTabs = computed(() => {
    return this.availableTabs()?.filter(each => !this.isTabVisible(each.id));
  });

}
