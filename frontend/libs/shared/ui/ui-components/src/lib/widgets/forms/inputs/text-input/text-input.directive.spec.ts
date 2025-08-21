import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TextInputDirective } from './text-input.directive';

describe('TextInputDirective', () => {
  describe('without id', () => {
    @Component({
      standalone: true,
      template: `<input svuiTextInput value="input value" />`,
      imports: [TextInputDirective],
      changeDetection: ChangeDetectionStrategy.OnPush,
    })
    class TestTextInputHostComponent {}

    let component: TestTextInputHostComponent;
    let fixture: ComponentFixture<TestTextInputHostComponent>;

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [TestTextInputHostComponent],
      }).compileComponents();

      fixture = TestBed.createComponent(TestTextInputHostComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => expect(component).toBeTruthy());

    describe('input ID attribute', () => {
      let input: HTMLInputElement;
      beforeEach(() => {
        input = fixture.nativeElement.querySelector('input');
      });

      it('is generated automatically according to naming-convention', () =>
        expect(input.getAttribute('id')).toMatch(/^auto-id-\d+$/));
    });
  });

  describe('with explicit id', () => {
    @Component({
      standalone: true,
      template: `<input svuiTextInput value="input value" id="asdf" />`,
      imports: [TextInputDirective],
      changeDetection: ChangeDetectionStrategy.OnPush,
    })
    class TestHostWithIDComponent {}

    let component: TestHostWithIDComponent;
    let fixture: ComponentFixture<TestHostWithIDComponent>;

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [TestHostWithIDComponent],
      }).compileComponents();

      fixture = TestBed.createComponent(TestHostWithIDComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => expect(component).toBeTruthy());

    describe('input ID attribute', () => {
      let input: HTMLInputElement;
      beforeEach(() => {
        input = fixture.nativeElement.querySelector('input');
      });

      it('keeps the assigned id', () =>
        expect(input.getAttribute('id')).toBe('asdf'));
    });
  });
});
