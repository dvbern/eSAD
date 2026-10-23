import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RadioDirective } from '../radio.directive';
import { SingleRadioWrapperComponent } from './single-radio-wrapper.component';

@Component({
  standalone: true,
  template: `
    <svui-single-radio-wrapper label="checkbox label">
      <input svuiRadio type="radio" value="input value" id="mycheckboxid" name="radio" />
    </svui-single-radio-wrapper>
  `,
  imports: [RadioDirective, SingleRadioWrapperComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
class TestHostComponent {}

describe('CheckWithLabelWrapperComponent', () => {
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

  describe('style classes', () =>
    it('has required label style class', () => {
      const label: HTMLLabelElement = fixture.nativeElement.querySelector('label');

      expect(label.classList.contains('form-check-label')).toBeTruthy();
    }));
});
