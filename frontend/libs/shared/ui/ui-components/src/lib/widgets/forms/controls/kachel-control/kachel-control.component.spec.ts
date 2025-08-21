import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {DetailMode} from '@dvb/util-angular';
import {CheckboxDirective} from '../../inputs/checkbox/checkbox.directive';
import {KachelControlComponent} from './kachel-control.component';

@Component({
  standalone: true,
  template: `
    <svui-kachel-control label="my label">
      <input type="checkbox" svuiCheckbox value="input value"/>
    </svui-kachel-control>
  `,
  imports: [CheckboxDirective, KachelControlComponent],
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
