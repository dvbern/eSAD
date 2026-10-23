import { DatePipe } from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, Signal} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {BenutzerOptionJson, UntersuchungImpfungenDoppelkontrolleSaveJson} from '@dvb/data-access-backend';
import {ISODateString, requireValidBackendEntityId, toEinladungBackendEntityIdWithClass} from '@dvb/data-access-types';
import {BenutzerOptionsStore, BenutzerStore, ConfirmService} from '@dvb/feat-shared';
import {
  ButtonComponent,
  FormControlComponent,
  FormLayoutComponent,
  HeadingDirective,
  NgSelectDirective,
  TextInputDirective,
} from '@dvb/ui-components';
import {AppNotificationStore, DetailMode} from '@dvb/util-angular';
import {compareFn, hasImpfungenEditBerechtigung, isNullish, Nullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {UntersuchungImpfungenStore} from '../untersuchung-impfungen.store';
import {UntersuchungDoppelkontrolleImpfentscheidForm} from './untersuchung-doppelkontrolle-impfentscheid.form';

type VisumStatus = 'Visum1Ausstehend' | 'Visum1Visiert' | 'Visum2Angefordert' | 'Visum2Visiert';

@Component({
  selector: 'feat-untersuchung-doppelkontrolle-impfentscheid',
  providers: [DatePipe],
  imports: [
    FormLayoutComponent,
    ReactiveFormsModule,
    HeadingDirective,
    TranslocoPipe,
    FormControlComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    ButtonComponent,
    TextInputDirective
],
  templateUrl: './untersuchung-doppelkontrolle-impfentscheid.component.html',
  styleUrl: './untersuchung-doppelkontrolle-impfentscheid.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungDoppelkontrolleImpfentscheidComponent {
  readonly anlassStore = inject(AnlassStore);
  readonly #untersuchungStore = inject(UntersuchungStore);
  readonly #untersuchungImpfungenStore = inject(UntersuchungImpfungenStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly #notificationStore = inject(AppNotificationStore);
  readonly #benutzerStore = inject(BenutzerStore);

  readonly #datePipe = inject(DatePipe);
  readonly #translocoService = inject(TranslocoService);
  readonly #confirmService = inject(ConfirmService);

  protected readonly compareFnBenutzer = compareFn<BenutzerOptionJson>;

  protected readonly DetailMode = DetailMode;

  canEdit = computed(
    () =>
      this.#benutzerStore.createHasRequiredRoleSig(hasImpfungenEditBerechtigung())() &&
      this.anlassStore.canUserEditAnlassInCurrentState(),
  );

  doppelkontrolleSig = this.#untersuchungImpfungenStore.doppelkontrolle;

  /**
   * Whether a Visum can be sent. Before the Impfungen are loaded the store cannot build the request and a
   * click would be lost without a trace, and a step sent while the previous one is still being saved carries
   * a stale version and is rejected as changed by someone else.
   */
  readyToSave = computed(
    () => this.#untersuchungImpfungenStore.versionUntersuchungImpfungen() !== undefined
      && !this.#untersuchungImpfungenStore.saveImpfungenLoader.isLoading(),
  );
  userExceptUserOfVisum1: Signal<BenutzerOptionJson[]> = computed(() => {
    return this.benutzerOptionsStore
      .entities()
      .filter(benutzer => benutzer.id !== this.doppelkontrolleSig()?.visumPerson1?.id);
  });

  status: VisumStatus = 'Visum1Ausstehend';

  form = new UntersuchungDoppelkontrolleImpfentscheidForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
  });

  constructor() {
    effect(() => {
      const data = this.doppelkontrolleSig();
      const currentBenutzerId: string | undefined = this.#benutzerStore.benutzer()?.id;
      const option = this.benutzerOptionsStore.getOptionForBenutzer(currentBenutzerId);
      this.form.resetForm(data, option);
      this.status = this.toVisumStatus(data);
      this.form.setDisabled(!this.canEdit());
    });
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      switch (this.status) {
        case 'Visum1Ausstehend':
          this.visierenDoppelkontrolleVisum1();
          break;
        case 'Visum1Visiert':
          this.anfordernDoppelkontrolleVisum2(payload);
          break;
        case 'Visum2Angefordert':
          this.visierenDoppelkontrolleVisum2(payload);
          break;
        case 'Visum2Visiert':
          this.resetDoppelkontrolle();
          break;
      }
    }
  }

  private visierenDoppelkontrolleVisum1(): void {
    this.#untersuchungImpfungenStore.visierenDoppelkontrolleVisum1({
      anlassId: requireValue(this.anlassStore.selectedEntity()).id,
      einladungId: requireValue(this.#untersuchungStore.currentTab()).einladungID,
    });
    this.status = 'Visum1Visiert';
  }

  private anfordernDoppelkontrolleVisum2(payload: UntersuchungImpfungenDoppelkontrolleSaveJson): void {
    if (payload.visumPerson1?.id === payload.visumPerson2?.id) {
      this.#notificationStore.addDangerNotification(
        this.#translocoService.translate('untersuchung.doppelkontrolle.same.person.error'),
      );
      return;
    }
    this.#untersuchungImpfungenStore.anfordernDoppelkontrolleVisum2({
      anlassId: requireValue(this.anlassStore.selectedEntity()).id,
      einladungId: requireValue(this.#untersuchungStore.currentTab()).einladungID,
      user: requireValue(payload.visumPerson2),
    });
    this.status = 'Visum2Angefordert';
  }

  private visierenDoppelkontrolleVisum2(payload: UntersuchungImpfungenDoppelkontrolleSaveJson): void {
    if (payload.visumPerson1?.id === this.#benutzerStore.benutzer()?.id) {
      this.#notificationStore.addDangerNotification(
        this.#translocoService.translate('untersuchung.doppelkontrolle.same.person.error'),
      );
      return;
    }
    this.#untersuchungImpfungenStore.visierenDoppelkontrolleVisum2({
      anlassId: requireValue(this.anlassStore.selectedEntity()).id,
      einladungId: requireValue(this.#untersuchungStore.currentTab()).einladungID,
    });
    this.status = 'Visum2Visiert';
  }

  private resetDoppelkontrolle(): void {
    this.#confirmService
      .confirm({
        question: this.#translocoService.translate('untersuchung.doppelkontrolle.reset.question'),
        consequence: this.#translocoService.translate('untersuchung.doppelkontrolle.reset.consequence'),
        decision: this.#translocoService.translate('untersuchung.doppelkontrolle.reset.decision'),
      })
      .subscribe(() => {
        this.#untersuchungImpfungenStore.resetDoppelkontrolle({
          anlassId: requireValue(this.anlassStore.selectedEntity()).id,
          einladungId: requireValue(this.#untersuchungStore.currentTab()).einladungID,
        });
        this.status = 'Visum1Visiert';
      });
  }

  getActionText(): string {
    switch (this.status) {
      case 'Visum1Ausstehend':
        return this.#translocoService.translate('untersuchung.doppelkontrolle.action.visieren');
      case 'Visum1Visiert':
        return this.#translocoService.translate('untersuchung.doppelkontrolle.action.zweitvisum.anfordern');
      case 'Visum2Angefordert':
        if (this.#benutzerStore.benutzer()?.id !== this.form.group.controls.visumPerson2.value?.id) {
          return this.#translocoService.translate('untersuchung.doppelkontrolle.action.zweitvisum.fremdvisieren', {
            user: this.form.group.controls.visumPerson2.value?.fullName,
          });
        }
        return this.#translocoService.translate('untersuchung.doppelkontrolle.action.visieren');
      case 'Visum2Visiert':
        return this.#translocoService.translate('untersuchung.doppelkontrolle.action.reset');
    }
  }

  getStatus(user: BenutzerOptionJson | Nullish, datum: ISODateString | Nullish) {
    if (user && !datum) {
      return this.#translocoService.translate('untersuchung.doppelkontrolle.status.angefordert', {
        user: user.fullName,
      });
    }
    if (!datum) {
      return this.#translocoService.translate('untersuchung.doppelkontrolle.status.nicht.visiert');
    }
    return this.#translocoService.translate('untersuchung.doppelkontrolle.status.visiert', {
      datum: this.#datePipe.transform(datum, 'dd.MM.yyyy'),
    });
  }

  private toVisumStatus(data: UntersuchungImpfungenDoppelkontrolleSaveJson | undefined): VisumStatus {
    if (isNullish(data)) {
      return 'Visum1Ausstehend';
    }
    if (!data?.visumDatum1) {
      return 'Visum1Ausstehend';
    }
    if (!data?.visumPerson2) {
      return 'Visum1Visiert';
    }
    if (!data?.visumDatum2) {
      return 'Visum2Angefordert';
    }
    return 'Visum2Visiert';
  }
}
