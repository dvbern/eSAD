import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RadioDirective } from './radio.directive';

@Component({
  standalone: true,
  template: `<input type="radio" name="the-name" svuiRadio value="input value" />`,
  imports: [RadioDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
class TestTextRadioHostComponent {}

describe('RadioInputDirective', () => {
  let component: TestTextRadioHostComponent;
  let fixture: ComponentFixture<TestTextRadioHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestTextRadioHostComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TestTextRadioHostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());

  it('has required style class', () => {
    const input: HTMLInputElement = fixture.nativeElement.querySelector('input');

    expect(input.classList.contains('form-check-input')).toBeTruthy();
  });
});
