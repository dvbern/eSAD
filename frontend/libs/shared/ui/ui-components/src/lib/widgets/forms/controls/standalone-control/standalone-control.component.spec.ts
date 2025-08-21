import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TextInputDirective } from '../../inputs/text-input/text-input.directive';
import { StandaloneControlComponent } from './standalone-control.component';

@Component({
  standalone: true,
  template: `
    <svui-standalone-control label="my label">
      <input svuiTextInput value="input value" />
    </svui-standalone-control>
  `,
  imports: [TextInputDirective, StandaloneControlComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
class TestHostComponent {}

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
