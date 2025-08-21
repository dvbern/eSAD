import {ChangeDetectionStrategy, Component} from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { StammdatenDispatchComponent } from './stammdaten-dispatch.component';

// this test takes a long time to run (30 min or more in pipeline) disable until we know why
describe('StammdatenDispatchComponent', () => {
  let component: StammdatenDispatchComponent;
  let fixture: ComponentFixture<StammdatenDispatchComponent>;

  @Component({ template: '' , changeDetection: ChangeDetectionStrategy.OnPush,})
  class FakeComponent {}

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        provideRouter([
          {
            path: ':id',
            component: FakeComponent,
          },
        ]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(StammdatenDispatchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to the correct route', async () => {
    fixture.elementRef.nativeElement.getElementsByTagName('a')[0].click();

    expect(TestBed.inject(Router).url).toEqual('/1');
  });
});
