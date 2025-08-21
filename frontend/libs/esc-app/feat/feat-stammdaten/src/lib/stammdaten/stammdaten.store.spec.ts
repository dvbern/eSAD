import { TestBed } from '@angular/core/testing';
import { Geschlecht } from './fake-stammdaten.service';
import { StammdatenStore } from './stammdaten.store';

describe('StammdatenStore', () => {
  describe('with initial state', () => {
    it('computes all properties as expected', () => {
      const store = TestBed.runInInjectionContext(() => new StammdatenStore());

      expect(store.isInitialized()).toEqual(false);
      expect(store.isLoaded()).toEqual(false);
      expect(store.stammdaten()).toBeUndefined();
    });
  });

  describe('with loaded state', () => {
    it('computes all properties as expected', () => {
      const store = TestBed.runInInjectionContext(
        () =>
          new StammdatenStore({
            id: '42',
            value: undefined,
            backendErrors: undefined,
          }),
      );

      expect(store.isInitialized()).toEqual(true);
      expect(store.isLoaded()).toEqual(false);
      expect(store.stammdaten()).toBeUndefined();
    });
  });

  describe('with loaded state', () => {
    it('computes all properties as expected', () => {
      const store = TestBed.runInInjectionContext(
        () =>
          new StammdatenStore({
            id: '42',
            value: {
              name: 'Mustermann',
              vorname: 'Max',
              geburtsDatum: '2000-01-01',
              geschlecht: Geschlecht.MANN,
            },
            backendErrors: undefined,
          }),
      );

      expect(store.isInitialized()).toEqual(true);
      expect(store.isLoaded()).toEqual(true);
      expect(store.stammdaten()).toEqual({
        name: 'Mustermann',
        vorname: 'Max',
        geburtsDatum: '2000-01-01',
        geschlecht: Geschlecht.MANN,
      });
    });
  });
});
