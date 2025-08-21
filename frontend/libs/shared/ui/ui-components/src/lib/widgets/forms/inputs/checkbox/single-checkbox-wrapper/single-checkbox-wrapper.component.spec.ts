import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {DetailMode} from '@dvb/util-angular';
import {provideTestingTransloco} from '@dvb/util-i18n';
import {CheckboxDirective} from '../../../inputs/checkbox/checkbox.directive';
import {FormLayoutComponent} from '../../../layout/form-layout/form-layout.component';
import {SingleCheckboxWrapperComponent} from './single-checkbox-wrapper.component';

@Component({
  standalone: true,
  template: `
    <form svuiFormLayout [mode]="DetailMode.EDIT">
      <svui-single-checkbox-wrapper label="checkbox label">
        <input svuiCheckbox type="checkbox" value="input value" id="mycheckboxid"/>
      </svui-single-checkbox-wrapper>
    </form>
  `,
  imports: [SingleCheckboxWrapperComponent, CheckboxDirective, FormLayoutComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
class TestHostComponent {
  protected readonly DetailMode = DetailMode;
}

describe('CheckWithLabelWrapperComponent', () => {
  let component: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [provideTestingTransloco()],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());

  describe('label for attribute', () => {
    let input: HTMLInputElement;
    beforeEach(() => {
      input = fixture.nativeElement.querySelector('input');
    });

    it('is set from the input', () => {
      const label = fixture.nativeElement.querySelector('label');

      expect(label.getAttribute('for')).toBe(input.getAttribute('id'));
    });
  });

  describe('style classes', () =>
    it('has required label style class', () => {
      const label: HTMLLabelElement = fixture.nativeElement.querySelector('label');

      expect(label.classList.contains('form-check-label')).toBeTruthy();
    }));
});
