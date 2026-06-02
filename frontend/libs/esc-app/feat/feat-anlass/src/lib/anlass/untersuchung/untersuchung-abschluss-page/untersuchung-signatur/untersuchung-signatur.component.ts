import {ChangeDetectionStrategy, Component, computed, inject} from "@angular/core";
import {ReactiveFormsModule} from "@angular/forms";
import {ButtonComponent, HeadingDirective} from "@dvb/ui-components";
import {TranslocoPipe, TranslocoService} from "@jsverse/transloco";
import {BenutzerStore, ConfirmService} from "@dvb/feat-shared";
import {UntersuchungStore} from "../../untersuchung.store";
import {RequirementSetting} from "@dvb/data-access-backend";
import {hasUntersuchungAbschlussEditBerechtigung, requireValue} from "@dvb/util-shared";
import {AnlassStore} from "../../../anlass.store";
import {UntersuchungSignaturStore} from "./untersuchung-signatur.store";
import {DatePipe} from "@angular/common";

@Component({
  selector: 'feat-untersuchung-signatur',
  imports: [
    ReactiveFormsModule,
    TranslocoPipe,
    HeadingDirective,
    ButtonComponent,
  ],
  providers: [DatePipe],
  templateUrl: './untersuchung-signatur.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungSignaturComponent {
  protected readonly RequirementSetting = RequirementSetting;

  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly store = inject(UntersuchungSignaturStore);
  readonly #benutzerStore = inject(BenutzerStore);
  readonly #confirmService = inject(ConfirmService);
  readonly #datePipe = inject(DatePipe);

  readonly #translocoService = inject(TranslocoService);

  canEditAbschluss = computed(() => this.#benutzerStore.createHasRequiredRoleSig(
      hasUntersuchungAbschlussEditBerechtigung())()
    && this.anlassStore.canUserEditAnlassInCurrentState());

  buttonText = computed(() => {
    if (this.store.untersuchungSignatur()?.data.person) {
      return this.#translocoService.translate('untersuchung.signatur.action.resetSignatur');
    } else {
      return this.#translocoService.translate('untersuchung.signatur.action.signieren');
    }
  });

  statusText = computed(() => {
    if (this.store.untersuchungSignatur()?.data.person) {
      return this.#translocoService.translate('untersuchung.signatur.status.signiert', {
        user: this.store.untersuchungSignatur()?.data.person?.fullName,
        datum: this.#datePipe.transform(this.store.untersuchungSignatur()?.data.timestampVisiert, 'dd.MM.yyyy HH:mm'),
      });
    } else {
      return this.#translocoService.translate('untersuchung.signatur.status.unsigniert');
    }
  });

  async changeSignatur(): Promise<void> {
    const anlassId = requireValue(this.anlassStore.selectedEntity()?.id);
    if (this.store.isSigniert()) {
      this.#confirmService.confirm({
        question: this.#translocoService.translate('untersuchung.signatur.delete.question'),
        consequence: this.#translocoService.translate('untersuchung.signatur.delete.consequence'),
        decision: this.#translocoService.translate('untersuchung.signatur.delete.decision'),
      }).subscribe(() => {
        this.store.resetSignatur({
          anlassId: anlassId,
          einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
        });
      });
    } else {
      this.store.signieren({
        anlassId: anlassId,
        einladungId: requireValue(this.untersuchungStore.getWrappedEinladungId()),
      });
    }
  }
}
