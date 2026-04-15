import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PublicinfoPageComponent } from './publicinfo-page.component';
import { provideTestingTransloco } from '@dvb/util-i18n';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('PublicinfoPageComponent', () => {
  let component: PublicinfoPageComponent;
  let fixture: ComponentFixture<PublicinfoPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PublicinfoPageComponent],
      providers: [
        provideTestingTransloco(),
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PublicinfoPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
