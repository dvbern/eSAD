import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ComponentRef} from '@angular/core';
import {provideRouter} from '@angular/router';
import {provideTestingTransloco} from '@dvb/util-i18n';
import {TenantEnum} from '@dvb/data-access-backend';
import {TenantFooterComponent} from './tenant-footer.component';

describe('TenantFooterComponent', () => {
  let component: TenantFooterComponent;
  let componentRef: ComponentRef<TenantFooterComponent>;
  let fixture: ComponentFixture<TenantFooterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TenantFooterComponent],
      providers: [
        provideTestingTransloco(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TenantFooterComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should not render logo image when tenant is not set', () => {
    fixture.detectChanges();
    const img: HTMLImageElement | null = fixture.nativeElement.querySelector('img');
    expect(img).toBeNull();
  });

  it('should render win logo with win class when tenant is WIN', () => {
    componentRef.setInput('tenant', TenantEnum.WIN);
    fixture.detectChanges();

    const img: HTMLImageElement | null = fixture.nativeElement.querySelector('img');
    expect(img).not.toBeNull();
    expect(img?.getAttribute('src')).toBe('img/logo-WIN.svg');
    expect(img?.classList.contains('win')).toBe(true);
  });

  it('should render BE logo without win class when tenant is BE', () => {
    componentRef.setInput('tenant', TenantEnum.BE);
    fixture.detectChanges();

    const img: HTMLImageElement | null = fixture.nativeElement.querySelector('img');
    expect(img).not.toBeNull();
    expect(img?.getAttribute('src')).toBe('img/logo-BE.svg');
    expect(img?.classList.contains('win')).toBe(false);
  });

  it('should render BS logo without win class when tenant is BS', () => {
    componentRef.setInput('tenant', TenantEnum.BS);
    fixture.detectChanges();

    const img: HTMLImageElement | null = fixture.nativeElement.querySelector('img');
    expect(img).not.toBeNull();
    expect(img?.getAttribute('src')).toBe('img/logo-BS.svg');
    expect(img?.classList.contains('win')).toBe(false);
  });
});
