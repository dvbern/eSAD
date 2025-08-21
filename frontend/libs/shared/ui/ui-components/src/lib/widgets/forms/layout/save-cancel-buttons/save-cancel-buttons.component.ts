import {ChangeDetectionStrategy, Component, computed, input, output} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';
import {ButtonComponent} from '../../../buttons';

@Component({
    selector: 'svui-save-cancel-buttons',
    templateUrl: './save-cancel-buttons.component.html',
    styleUrl: './save-cancel-buttons.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        ButtonComponent,
        TranslocoPipe
    ]
})
export class SaveCancelButtonsComponent {

  // submit button has a loading spinner
  saving = input.required<boolean>();

  // canEdit can be false if a user has no permissions or if there are other reasons why the item is not editable
  canEdit = input.required<boolean>();

  primaryButtonText = input<string>();

  primaryButtonTextOrDefault = computed(() => {
    if (this.primaryButtonText()) {
      return this.primaryButtonText();
    }
    return 'global.save';
  });

  cancelClicked = output<void>();

  public cancel(): void {
    this.cancelClicked.emit();
  }
}
