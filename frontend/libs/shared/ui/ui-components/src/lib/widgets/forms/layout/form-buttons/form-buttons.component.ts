import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DetailMode } from '@dvb/util-angular';
import { TranslocoPipe } from '@jsverse/transloco';
import { ButtonComponent, LikeAButtonComponent } from '../../../buttons';

@Component({
    selector: 'svui-form-buttons',
    templateUrl: './form-buttons.component.html',
    styleUrl: './form-buttons.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [ButtonComponent, LikeAButtonComponent, RouterLink, TranslocoPipe]
})
export class FormButtonsComponent {
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);

  // mode: view -> show edit button; edit -> show submit and cancel buttons
  mode = input.required<DetailMode>();
  isViewMode = computed(() => {
    return this.mode() === DetailMode.VIEW;
  });
  isEditMode = computed(() => {
    return !this.isViewMode();
  });

  // submit button has a loading spinner
  saving = input.required<boolean>();

  // canEdit can be false if a user has no permissions or if there are other reasons why the item is not editable
  canEdit = input.required<boolean>();

  // from view mode to edit mode
  routeToEdit = input.required<string>();

  // edit mode: cancel button for create/update
  isCreate = input.required<boolean>();
  routeOnCancelUpdate = input.required<string[]>();
  routeOnCancelCreate = input.required<string[]>();

  public cancel(): void {
    if (this.isCreate()) {
      this.#router.navigate(this.routeOnCancelCreate(), { relativeTo: this.#activatedRoute });
    } else {
      this.#router.navigate(this.routeOnCancelUpdate(), { relativeTo: this.#activatedRoute });
    }
  }
}
