import { TestBed } from '@angular/core/testing';
import { confirmOnDirty } from './confirmOnDirty.candeactivatefn';
import { DirtyGuardService } from './dirty-guard.service';
import { DirtyMarker } from './dirty-marker';

describe('confirmOnDirty', () => {
  const PristineComponent: DirtyMarker = {
    isDirty: () => false,
  };
  const DirtyComponent: DirtyMarker = {
    isDirty: () => true,
  };

  class MockDirtyGuardService implements Partial<DirtyGuardService> {
    askForNavigationOverride = jest.fn().mockRejectedValue('not initialized');
  }

  const mockService: MockDirtyGuardService = new MockDirtyGuardService();

  describe('for a pristine component', () => {
    beforeEach(() => {
      mockService.askForNavigationOverride = jest.fn().mockRejectedValue('should not have been called');

      TestBed.configureTestingModule({
        providers: [
          {
            provide: DirtyGuardService,
            useValue: mockService,
          },
        ],
      });
    });

    it('returns true and does not call the service', async () => {
      const guard = confirmOnDirty();
      const outcome = await TestBed.runInInjectionContext(() =>
        guard(PristineComponent, undefined as any, undefined as any, undefined as any),
      );

      expect(mockService.askForNavigationOverride).not.toHaveBeenCalled();
      expect(outcome).toBe(true);
    });
  });

  describe('for a dirty component', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          {
            provide: DirtyGuardService,
            useValue: mockService,
          },
        ],
      });
    });

    it.each([true, false])('returns the service outcome (user allows navigation): %p', async navigationAllowed => {
      mockService.askForNavigationOverride = jest.fn().mockResolvedValue(navigationAllowed);

      const guard = confirmOnDirty();
      const outcome = await TestBed.runInInjectionContext(() =>
        guard(DirtyComponent, undefined as any, undefined as any, undefined as any),
      );

      expect(mockService.askForNavigationOverride).toHaveBeenCalled();
      expect(outcome).toBe(navigationAllowed);
    });
  });

  describe('for a component not implementing DirtyMarker', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          {
            provide: DirtyGuardService,
            useValue: mockService,
          },
        ],
      });
    });

    it.each([
      [null, 'object/undefined'],
      [{}, 'object/Object'],
      [new (class Foobar {})(), 'object/Foobar'],
    ])('throws an error on input %p with debug message %s:', async (input, debugMessage) => {
      const guard = confirmOnDirty();

      await expect(
        TestBed.runInInjectionContext(() => guard(input, undefined as any, undefined as any, undefined as any)),
      ).rejects.toThrow(`Component does not implement DirtyGuard: ${debugMessage}`);
    });
  });
});
