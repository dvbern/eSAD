import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {DetailMode} from '@dvb/util-angular';
import {provideTestingTransloco} from '@dvb/util-i18n';
import {TextInputDirective} from '../inputs/text-input/text-input.directive';
import {FormLayoutComponent} from '../layout/form-layout/form-layout.component';
import {FormControlComponent} from './form-control/form-control.component';

@Component({
  standalone: true,
  template: `
    <form svuiFormLayout [mode]="DetailMode.EDIT">
      <svui-form-control label="my label" [viewValue]="">
        <input type="text" svuiTextInput [formControl]="control"/>
      </svui-form-control>
    </form>
  `,
  imports: [FormControlComponent, TextInputDirective, FormLayoutComponent, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
class TestHostComponent {
  protected readonly DetailMode = DetailMode;
  readonly control = new FormControl<string | null>('');
}

describe('ValidatedControlWrapperDirective', () => {
  let component: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  const queryRequiredMarker = (): HTMLElement | null =>
    fixture.nativeElement.querySelector('svui-required');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [provideTestingTransloco()],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => expect(component)
    .toBeTruthy());

  it('renders no required marker for a control without a required validator', () => {
    expect(queryRequiredMarker())
      .toBeNull();
  });

  // regression: the required flag used to be a plain field, so the marker only appeared because
  // the recursive validity check happened to run detectChanges() on the very same view
  it('renders the required marker once a required validator is added', async () => {
    component.control.addValidators(Validators.required);
    component.control.updateValueAndValidity();

    await fixture.whenStable();

    expect(queryRequiredMarker())
      .not
      .toBeNull();
  });

  // the validity trigger exists to render control.touched / control.errors, which are plain
  // template reads rather than signals - so it must still mark the view dirty on blur
  it('renders the validation error once the control is touched', async () => {
    component.control.addValidators(Validators.required);
    component.control.updateValueAndValidity();
    await fixture.whenStable();

    component.control.markAsTouched();
    component.control.updateValueAndValidity();
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('svui-form-control-error'))
      .not
      .toBeNull();
  });

  it('removes the required marker again once the required validator is dropped', async () => {
    component.control.addValidators(Validators.required);
    component.control.updateValueAndValidity();
    await fixture.whenStable();

    component.control.removeValidators(Validators.required);
    component.control.updateValueAndValidity();
    await fixture.whenStable();

    expect(queryRequiredMarker())
      .toBeNull();
  });
});
