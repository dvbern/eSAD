import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {DetailMode} from '@dvb/util-angular';
import {TextInputDirective} from '../../inputs/text-input/text-input.directive';
import {FormLayoutComponent} from '../../layout/form-layout/form-layout.component';
import {FormControlComponent} from './form-control.component';

@Component({
  standalone: true,
  template: `
    <form svuiFormLayout [mode]="DetailMode.EDIT">
      <svui-form-control label="my label" [viewValue]="">
        <input type="text" svuiTextInput value="input value"/>
      </svui-form-control>
    </form>
  `,
  imports: [FormControlComponent, TextInputDirective, FormLayoutComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
class TestHostComponent {
  protected readonly DetailMode = DetailMode;
}

describe('ControlWrapperComponent', () => {
  let component: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
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
});
