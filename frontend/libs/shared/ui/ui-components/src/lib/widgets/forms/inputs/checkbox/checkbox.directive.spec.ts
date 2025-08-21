import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {DetailMode} from '@dvb/util-angular';
import {provideTestingTransloco} from '@dvb/util-i18n';
import {StandaloneControlComponent} from '../../controls/standalone-control/standalone-control.component';
import {CheckboxDirective} from './checkbox.directive';

@Component({
  standalone: true,
  template: `
    <svui-standalone-control label="my label">
      <input svuiCheckbox type="checkbox"/>
    </svui-standalone-control>
  `,
  imports: [CheckboxDirective, StandaloneControlComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
class TestHostComponent {
  protected readonly DetailMode = DetailMode;
}

describe('CheckboxDirective', () => {
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
});
