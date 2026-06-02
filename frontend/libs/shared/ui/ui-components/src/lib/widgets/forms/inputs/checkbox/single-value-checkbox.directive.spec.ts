import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {provideTestingTransloco} from '@dvb/util-i18n';
import {StandaloneControlComponent} from '../../controls/standalone-control/standalone-control.component';
import {SingleValueCheckboxDirective} from './single-value-checkbox.directive';

@Component({
  standalone: true,
  template: `
    <svui-standalone-control label="my label">
      <input svuiSingleValueCheckbox
             type="checkbox"
             [value]="value"
             [formControl]="control"/>
    </svui-standalone-control>
  `,
  imports: [SingleValueCheckboxDirective, StandaloneControlComponent, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
class TestHostComponent {
  readonly value: string = 'NICHT_GEPRUEFT';
  readonly control: FormControl<string | undefined> = new FormControl<string | undefined>(undefined,
    {nonNullable: true});
}

describe('SingleValueCheckboxDirective', () => {
  let component: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;
  let input: HTMLInputElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [provideTestingTransloco(), NonNullableFormBuilder],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    input = fixture.nativeElement.querySelector('input');
  });

  it('should create', () => expect(component).toBeTruthy());

  it('label `for` attribute is set from the input id', () => {
    const label = fixture.nativeElement.querySelector('label');
    expect(label.getAttribute('for')).toBe(input.getAttribute('id'));
  });

  it('checkbox is unchecked when control value is undefined', () => {
    expect(input.checked).toBe(false);
  });

  it('checkbox is checked when control value matches [value]', () => {
    component.control.setValue('NICHT_GEPRUEFT');
    fixture.detectChanges();
    expect(input.checked).toBe(true);
  });

  it('checkbox is unchecked when control value differs from [value]', () => {
    component.control.setValue('OTHER_VALUE');
    fixture.detectChanges();
    expect(input.checked).toBe(false);
  });

  it('sets control value to [value]-string when checked via user interaction', () => {
    input.checked = true;
    input.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(component.control.value).toBe('NICHT_GEPRUEFT');
  });

  it('sets control value to undefined when unchecked via user interaction', () => {
    component.control.setValue('NICHT_GEPRUEFT');
    fixture.detectChanges();
    input.checked = false;
    input.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(component.control.value).toBeUndefined();
  });

  it('marks control as touched on blur', () => {
    expect(component.control.touched).toBe(false);
    input.dispatchEvent(new Event('blur'));
    expect(component.control.touched).toBe(true);
  });

  it('marks control as touched on change', () => {
    expect(component.control.touched).toBe(false);
    input.checked = true;
    input.dispatchEvent(new Event('change'));
    expect(component.control.touched).toBe(true);
  });

  it('disables input when control is disabled', () => {
    component.control.disable();
    fixture.detectChanges();
    expect(input.disabled).toBe(true);
  });
});
