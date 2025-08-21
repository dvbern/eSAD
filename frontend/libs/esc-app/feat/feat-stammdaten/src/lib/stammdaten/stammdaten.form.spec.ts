import { TestBed } from '@angular/core/testing';
import {DetailMode} from '@dvb/util-angular';
import { ensureProps } from '@dvb/util-shared';
import { MyStammdatenDTO } from './fake-stammdaten.service';
import { StammdatenForm } from './stammdaten.form';

describe('StammdatenForm', () => {
  describe('initially', () => {
    let form: StammdatenForm;

    beforeEach(() => {
      form = TestBed.runInInjectionContext(() => new StammdatenForm());
    });

    it('has the expected initial value', () => {
      const actual = form.group.getRawValue();

      expect(actual).toEqual(
        ensureProps<StammdatenForm['valueOnValid']>({
          vorname: '',
          name: '',
          geburtsDatum: '',
          geschlecht: '',
        }),
      );
      expect(form.isDirty()).toBe(false);
    });
  });

  describe('when resetting ta valid value', () => {
    let form: StammdatenForm;

    beforeEach(() => {
      form = TestBed.runInInjectionContext(() => new StammdatenForm());

      form.reset(
        ensureProps<MyStammdatenDTO>({
          name: 'Hans',
          vorname: 'Dampf',
          geburtsDatum: '1976-11-19',
          geschlecht: 'MANN',
        }),
        DetailMode.EDIT,
        undefined,
      );
    });

    it('is valid', async () => {
      const actual = await form.validate();

      expect(actual).toBe(true);
    });

    it('returns the converted value', async () => {
      const actual = form.valueOnValid();

      expect(actual).toEqual(
        ensureProps<MyStammdatenDTO>({
          name: 'Hans',
          vorname: 'Dampf',
          geburtsDatum: '1976-11-19',
          geschlecht: 'MANN',
        }),
      );
    });
  });
  describe('when resetting to a partial but valid value', () => {
    let form: StammdatenForm;

    beforeEach(() => {
      form = TestBed.runInInjectionContext(() => new StammdatenForm());

      form.reset(
        ensureProps<MyStammdatenDTO>({
          name: 'Hans',
          vorname: 'Dampf',
          geburtsDatum: undefined,
          geschlecht: 'MANN',
        }),
        DetailMode.EDIT,
        undefined,
      );
    });

    it('is valid', async () => {
      const actual = await form.validate();

      expect(actual).toBe(true);
    });

    it('returns the converted value', async () => {
      const actual = form.valueOnValid();

      expect(actual).toEqual(
        ensureProps<MyStammdatenDTO>({
          name: 'Hans',
          vorname: 'Dampf',
          geburtsDatum: undefined,
          geschlecht: 'MANN',
        }),
      );
    });
  });
});
