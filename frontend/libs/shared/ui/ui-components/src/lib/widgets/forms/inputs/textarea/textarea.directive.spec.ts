import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TextareaDirective } from './textarea.directive';

describe('TextInputLargeDirective', () => {
  describe('without id', () => {
    @Component({
      standalone: true,
      template: `<textarea svuiTextarea></textarea>`,
      imports: [TextareaDirective],
      changeDetection: ChangeDetectionStrategy.OnPush,
    })
    class TestTextInputLargeHostComponent {}

    let component: TestTextInputLargeHostComponent;
    let fixture: ComponentFixture<TestTextInputLargeHostComponent>;

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [TestTextInputLargeHostComponent],
      }).compileComponents();

      fixture = TestBed.createComponent(TestTextInputLargeHostComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => expect(component).toBeTruthy());

    describe('textarea ID attribute', () => {
      let textarea: HTMLTextAreaElement;
      beforeEach(() => {
        textarea = fixture.nativeElement.querySelector('textarea');
      });

      it('is generated automatically according to naming-convention', () =>
        expect(textarea.getAttribute('id')).toMatch(/^auto-id-\d+$/));
    });
  });

  describe('with explicit id', () => {
    @Component({
      standalone: true,
      template: `<textarea svuiTextarea id="asdf"></textarea>`,
      imports: [TextareaDirective],
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

    describe('textarea ID attribute', () => {
      let textarea: HTMLTextAreaElement;
      beforeEach(() => {
        textarea = fixture.nativeElement.querySelector('textarea');
      });

      it('keeps the assigned id', () =>
        expect(textarea.getAttribute('id')).toBe('asdf'));
    });
  });
});
