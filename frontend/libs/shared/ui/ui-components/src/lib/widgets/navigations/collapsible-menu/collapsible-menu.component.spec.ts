import {ComponentRef} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideTestingTransloco} from '@dvb/util-i18n';
import {CollapsibleMenuComponent} from './collapsible-menu.component';

describe('CollapsibleMenuComponent', () => {
  let component: CollapsibleMenuComponent;
  let componentRef: ComponentRef<CollapsibleMenuComponent>;
  let fixture: ComponentFixture<CollapsibleMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CollapsibleMenuComponent],
      providers: [provideTestingTransloco()],
    }).compileComponents();

    fixture = TestBed.createComponent(CollapsibleMenuComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('label', 'Action menu');
    componentRef.setInput('loading', false);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component)
      .toBeTruthy();
  });

  it('should render a transparent secondary toggle without explicit buttonColor', () => {
    expect(component.buttonColor())
      .toBeUndefined();

    const hostElement: HTMLElement = fixture.nativeElement;
    expect(hostElement.classList.contains('svui_collapsible-menu--transparent'))
      .toBe(true);

    const buttonElement = hostElement.querySelector('button');
    expect(buttonElement?.classList.contains('svui_icon-button--secondary'))
      .toBe(true);
  });

  it('should apply the configured buttonColor without transparency', () => {
    componentRef.setInput('buttonColor', 'tableRowAction');
    fixture.detectChanges();

    const hostElement: HTMLElement = fixture.nativeElement;
    expect(hostElement.classList.contains('svui_collapsible-menu--transparent'))
      .toBe(false);

    const buttonElement = hostElement.querySelector('button');
    expect(buttonElement?.classList.contains('svui_icon-button--table-row-action'))
      .toBe(true);
  });
});
