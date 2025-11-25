
import {ChangeDetectionStrategy, Component, DestroyRef, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {Router} from '@angular/router';
import {KindSearchJson} from '@dvb/data-access-backend';
import {TranslocoPipe} from '@jsverse/transloco';
import {distinctUntilChanged} from 'rxjs';
import {Path} from '../../../core/path';
import {KindSearchComponent} from '../../../kind-search/kind-search.component';

@Component({
  selector: 'feat-page-global-search',
  imports: [
    KindSearchComponent,
    TranslocoPipe
],
  templateUrl: './global-search.component.html',
  styleUrl: './global-search.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GlobalSearchComponent {
  fb = inject(NonNullableFormBuilder);
  destroyRef = inject(DestroyRef);
  router = inject(Router);

  form = this.fb.group({
    globalSearch: this.fb.control<KindSearchJson | null>(null, []),
  });

  constructor() {
    this.form.controls.globalSearch.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: kind => {
          if (kind) {
            this.navigateToKind(kind);
            this.form.reset();
          }
        },
        error: console.error,
      });
  }

  public navigateToKind($event: KindSearchJson): void {
    console.log('navigating to Kind', $event);
    this.router.navigate([Path.KINDER, $event.id]);
  }
}
