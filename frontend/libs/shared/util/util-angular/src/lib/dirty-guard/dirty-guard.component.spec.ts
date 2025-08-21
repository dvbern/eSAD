import {ComponentFixture, TestBed} from '@angular/core/testing';
import {DirtyGuardComponent} from './dirty-guard.component';
import {DirtyGuardService} from './dirty-guard.service';

describe('DirtyGuardComponent', () => {
  let component: DirtyGuardComponent;
  let fixture: ComponentFixture<DirtyGuardComponent>;
  let service: DirtyGuardService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DirtyGuardComponent],
    }).compileComponents();

    service = TestBed.inject(DirtyGuardService);
    fixture = TestBed.createComponent(DirtyGuardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('initially', () => {
    it('should not be visible', () => {
      expect(fixture.debugElement.children)
        .toEqual([]);
    })
  });

  // Funktioniert mit einem Modal; Test sind aus dem AngularStarter Projekt
  describe.skip('when guard asks for confirmation', () => {
    let outcome: Promise<boolean>;

    let continueButton: HTMLButtonElement;
    let goBackButton: HTMLButtonElement;

    beforeEach(async () => {
      outcome = service.askForNavigationOverride();
      fixture.detectChanges();

      continueButton = (fixture.debugElement.nativeElement as HTMLElement)
        .querySelector('button[data-testid="continue"]') as HTMLButtonElement;
      goBackButton = (fixture.debugElement.nativeElement as HTMLElement)
        .querySelector('button[data-testid="back"]') as HTMLButtonElement
    })

    it('shows the dialog', async () => {
      expect(fixture.debugElement.children)
        .not.toEqual([])

      expect(continueButton)
        .toBeTruthy();
      expect(goBackButton)
        .toBeTruthy();
    });

    describe('when the user clicks "Continue"', () => {
      beforeEach(() => {
        continueButton.click();
        fixture.detectChanges()
      });

      it('resolves the outcome to true', async () => {
        const actual = await outcome;

        expect(actual)
          .toBe(true);
      })

      it('hides the dialog content', () => {
        expect(fixture.debugElement.children)
          .toEqual([]);
      })
    })
    describe('when the user clicks "Go Back"', () => {
      beforeEach(() => {
        goBackButton.click();
        fixture.detectChanges()
      });

      it('resolves the outcome to false', async () => {
        const actual = await outcome;

        expect(actual)
          .toBe(false);
      })

      it('hides the dialog content', () => {
        expect(fixture.debugElement.children)
          .toEqual([]);
      })
    })
  })
});
