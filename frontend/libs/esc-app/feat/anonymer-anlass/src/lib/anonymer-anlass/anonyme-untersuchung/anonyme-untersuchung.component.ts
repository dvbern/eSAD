import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {ConfirmService, Path} from '@dvb/feat-shared';
import {AppNotificationStore, DirtyMarker} from '@dvb/util-angular';
import {NgbNavModule} from '@ng-bootstrap/ng-bootstrap';
import {AnlassStore, UntersuchungStore, UntersuchungTabsComponent} from '@dvb/feat-anlass';
import {AnonymerEintragStore} from '../anonymer-eintrag.store';
import {requireValue} from '@dvb/util-shared';
import {TranslocoService} from '@jsverse/transloco';
import {Router} from '@angular/router';

@Component({
  selector: 'feat-anonymer-anlass-anonyme-untersuchung',
  templateUrl: './anonyme-untersuchung.component.html',
  styleUrl: './anonyme-untersuchung.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    NgbNavModule,
    UntersuchungTabsComponent,
  ],
})
export class AnonymeUntersuchungComponent {
  readonly #store = inject(AnonymerEintragStore);
  readonly #anlassStore = inject(AnlassStore);
  readonly #untersuchungStore = inject(UntersuchungStore);

  readonly #confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #router = inject(Router);

  protected untersuchungAbschliessen() {
    this.#confirmService.confirm({
      question: this.#transloco.translate('untersuchung.anonym.tab.abschliessen.question'),
      consequence: this.#transloco.translate('untersuchung.anonym.tab.abschliessen.consequence'),
      decision: this.#transloco.translate('untersuchung.anonym.tab.abschliessen.decision'),
    }).subscribe(() => {
      this.#store.abschliessenAnonymerEintrag({
        anlassId: requireValue(this.#anlassStore.selectedEntity()?.id),
        anonymerEintragId: requireValue(this.#untersuchungStore.getWrappedEinladungId()),
        onSuccess: () => {
          this.#appNotificationStore.addSuccessNotification(
            this.#transloco.translate('untersuchung.anonym.tab.abschliessen.success'),
          );
          this.#router.navigate(['/' + Path.ANONYM_ANLASS]);
        }
      })
    });
  }
}
