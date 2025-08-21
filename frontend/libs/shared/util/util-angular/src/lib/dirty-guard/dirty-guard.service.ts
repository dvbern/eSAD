import {Injectable} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {BehaviorSubject, filter, firstValueFrom, map} from 'rxjs';

@Injectable({providedIn: 'root'})
export class DirtyGuardService {

  readonly #canNavigateSubject = new BehaviorSubject<'disabled' | 'show-popup' | boolean>('disabled');
  readonly #canNavigate = this.#canNavigateSubject.pipe(
    takeUntilDestroyed(),
  );
  readonly #outcome = this.#canNavigate.pipe(
    filter(value => typeof value === 'boolean'),
  );
  showPopup = this.#canNavigate.pipe(
    map(value => value === 'show-popup'),
  );

  askForNavigationOverride(): Promise<boolean> {
    this.#canNavigateSubject.next('show-popup');

    return firstValueFrom(this.#outcome);
  }

  allowNavigation(): void {
    this.#canNavigateSubject.next(true);
    this.#canNavigateSubject.next('disabled');
  }

  preventNavigation(): void {
    this.#canNavigateSubject.next(false);
    this.#canNavigateSubject.next('disabled');
  }
}
