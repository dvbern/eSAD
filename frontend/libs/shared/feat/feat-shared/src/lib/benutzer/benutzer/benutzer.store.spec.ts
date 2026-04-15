import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {BenutzerService} from '@dvb/data-access-backend';
import {ensureProps} from '@dvb/util-shared';
import {AppUser, BenutzerStore} from './benutzer.store';

describe('BenutzerStore', () => {
  let store: BenutzerStore;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        BenutzerStore,
        BenutzerService,
      ],
    });

    store = TestBed.inject(BenutzerStore);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  describe('with initial state', () => {
    it('should be logged out', () => {
      expect(store.isLoggedIn()).toBe(false);
      expect(store.isLoggedOut()).toBe(true);

      expect(store.benutzer()).toBeUndefined();
      expect(() => store.requireBenutzer()).toThrow();
    });
  });

  describe('onUserAuthenticated', () => {
    it('should patch the state with the user details', () => {
      const store: BenutzerStore = TestBed.runInInjectionContext(() => new BenutzerStore({
        user: undefined,
        errorMessage: undefined,
      }));

      store.onUserAuthenticated('id',
        'username',
        'email',
        'firstName',
        'lastName',
        [],
        undefined,
        ['ANLASSPLANUNG_LESEN'],
        undefined
      );

      expect(store.isLoggedIn())
        .toBe(true);
      expect(store.isLoggedOut())
        .toBe(false);

      const expectedUser = ensureProps<AppUser>({
        id: 'id',
        username: 'username',
        email: 'email',
        firstName: 'firstName',
        lastName: 'lastName',
        fullName: 'firstName lastName',
        kuerzel: 'FL',
        fachRollen: [],
        berechtigungen: ['ANLASSPLANUNG_LESEN'],
      });
      expect(store.benutzer())
        .toEqual(expectedUser);
      expect(store.requireBenutzer())
        .toEqual(expectedUser);
    });
  });
});
