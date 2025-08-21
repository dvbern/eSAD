import {ChangeDetectionStrategy, Component, inject, TemplateRef, ViewChild} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ConfirmService, EditDialogService} from '@dvb/feat-shared';
import {
  ButtonComponent,
  CardCollapsibleComponent,
  CardComponent,
  FormControlComponent,
  FormLayoutComponent,
  HeadingDirective,
  TextInputDirective,
} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {PlaygroundFormInputComponent} from '../playground-form-inputs/playground-form-input.component';
import {PlaygroundEditDialogForm} from './playground-edit-dialog-form';

@Component({
    selector: 'feat-playground-dialogs',
    templateUrl: './playground-dialogs.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        ButtonComponent,
        FormLayoutComponent,
        FormControlComponent,
        FormsModule,
        ReactiveFormsModule,
        CardComponent,
        TextInputDirective,
        CardCollapsibleComponent,
        HeadingDirective,
    ]
})
export class PlaygroundDialogsComponent {
  confirmService = inject(ConfirmService);
  editDialogService = inject(EditDialogService);

  editForm = new PlaygroundEditDialogForm();

  @ViewChild('editFormTemplate', {read: TemplateRef}) editFormTemplateRef!: TemplateRef<PlaygroundFormInputComponent>;

  showConfirm(): void {
    this.confirmService
      .confirm({
        question: 'Question',
        consequence: 'This paragraph describes the consequence',
        wayOut: 'way out',
        decision: 'decision',
      })
      .subscribe(_ => console.log('we will proceed'));
  }

  resetForm(): void {
    this.editForm.reset();
  }

  showEditDialog(): void {
    this.editForm.group.reset(this.editForm.group.value); // be pristine but keep previous content

    this.editDialogService
      .edit({
        heading: 'Title',
        content: this.editFormTemplateRef,
        form: this.editForm,
        wayOut: 'way out',
        save: 'Save',
        size: 'xl',
      })
      .subscribe(outcome => console.log('we will proceed', outcome));
  }

  protected readonly DetailMode = DetailMode;
}
