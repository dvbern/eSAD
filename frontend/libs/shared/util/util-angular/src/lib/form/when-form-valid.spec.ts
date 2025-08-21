import {DestroyRef} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {FormGroup, NonNullableFormBuilder, Validators} from '@angular/forms';
import {whenFormValid} from './when-form-valid';

describe('whenFormValid', () => {
  let fb: NonNullableFormBuilder;
  const fakeDestroyRef: DestroyRef = {
    onDestroy: jest.fn(),
  };

  function makeGroup(): FormGroup {
    return fb.group({
      name: fb.control('', [Validators.maxLength(2)]),
    });
  }

  function okValue() {
    return {name: 'ok'};
  }

  function invalidValue() {
    return {name: 'invalid'};
  }

  beforeEach(async () => {
    fb = TestBed.inject(NonNullableFormBuilder);
  });

  describe('when form is disabled', () => {
    it('returns false', async () => {
      const form = makeGroup();
      form.disable();

      const valid = await whenFormValid(form, fakeDestroyRef);

      expect(valid)
        .toBe(false);
    });
  });

  describe('when form is valid', () => {

    let form: FormGroup;
    let valid: boolean;

    beforeEach(async () => {
      form = makeGroup();
      form.setValue(okValue());

      valid = await whenFormValid(form, fakeDestroyRef);
    });

    it('returns true', () => {
      expect(valid)
        .toBe(true);
    });

    it('marks all form controls as touched', () => {
      expect(form.touched)
        .toBe(true);
    });
  });

  describe('when form is not valid', () => {
    let form: FormGroup;
    let valid: boolean;

    beforeEach(async () => {
      form = makeGroup();
      form.setValue(invalidValue());

      form.setErrors({invalid: true});

      valid = await whenFormValid(form, fakeDestroyRef);
    });

    it('returns false', () => {
      console.log('valid', form.status, valid);

      expect(valid)
        .toBe(false);
    });

    it('marks all form controls as touched', () => {
      expect(form.touched).toBe(true);
    });
  });
});
