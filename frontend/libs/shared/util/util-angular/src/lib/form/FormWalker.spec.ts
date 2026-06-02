import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { FormWalker, Visitor, VisitorContext } from './FormWalker';

describe('FormWalker', () => {
  const fb = new FormBuilder();

  // a form group with arrays and groups and controls
  const group = fb.group({
    first: true,
    name: 'John',
    age: 42,
    address: fb.group({
      street: '123 Main St',
      city: 'Springfield',
    }),
    phones: fb.array([
      fb.group({
        type: 'home',
        number: '555-1212',
      }),
      fb.group({
        type: 'work',
        number: '555-1234',
      }),
    ]),
    last: true,
  });

  class ToArrayVisitor implements Visitor {
    visited: VisitorContext<AbstractControl>[] = [];

    onFormGroup(opts: VisitorContext<FormGroup>): void {
      this.visited.push(opts);
    }

    onFormArray(opts: VisitorContext<FormArray>): void {
      this.visited.push(opts);
    }

    onFormControl(opts: VisitorContext<FormControl<unknown>>): void {
      this.visited.push(opts);
    }
  }

  it('should visit all controls depth-first/top-down in order', () => {
    const visitor = new FormWalker().walkDepthFirst({
      root: group,
      descentStrategy: 'top-down',
      visitor: new ToArrayVisitor(),
    });

    expect(visitor.visited.map(v => `${v.path.padEnd(20)} ${v.name.padEnd(10)} ${v.control.constructor.name}`)).toEqual(
      [
        '                                FormGroup',
        'first                first      FormControl2',
        'name                 name       FormControl2',
        'age                  age        FormControl2',
        'address              address    FormGroup',
        'address.street       street     FormControl2',
        'address.city         city       FormControl2',
        'phones               phones     FormArray',
        'phones[0]            0          FormGroup',
        'phones[0].type       type       FormControl2',
        'phones[0].number     number     FormControl2',
        'phones[1]            1          FormGroup',
        'phones[1].type       type       FormControl2',
        'phones[1].number     number     FormControl2',
        'last                 last       FormControl2',
      ],
    );
  });

  it('should visit all controls depth-first/bottom-up in order', () => {
    const visitor = new FormWalker().walkDepthFirst({
      root: group,
      descentStrategy: 'bottom-up',
      visitor: new ToArrayVisitor(),
    });

    expect(visitor.visited.map(v => `${v.path.padEnd(20)} ${v.name.padEnd(10)} ${v.control.constructor.name}`)).toEqual(
      [
        'first                first      FormControl2',
        'name                 name       FormControl2',
        'age                  age        FormControl2',
        'address.street       street     FormControl2',
        'address.city         city       FormControl2',
        'address              address    FormGroup',
        'phones[0].type       type       FormControl2',
        'phones[0].number     number     FormControl2',
        'phones[0]            0          FormGroup',
        'phones[1].type       type       FormControl2',
        'phones[1].number     number     FormControl2',
        'phones[1]            1          FormGroup',
        'phones               phones     FormArray',
        'last                 last       FormControl2',
        '                                FormGroup',
      ],
    );
  });

  describe('with unsupported FormControl', () => {
    it('throws an error', async () => {
      class MyControl extends AbstractControl {
        override setValue(_value: unknown, _options?: unknown): void {
          throw new Error('Method not implemented.');
        }

        override patchValue(_value: unknown, _options?: unknown): void {
          throw new Error('Method not implemented.');
        }

        override reset(_value?: unknown, _options?: unknown): void {
          throw new Error('Method not implemented.');
        }

        constructor() {
          super(null, null);
        }
      }

      const form = fb.group({
        foo: new MyControl(),
      });

      expect(() =>
        new FormWalker().walkDepthFirst({
          root: form,
          descentStrategy: 'bottom-up',
          visitor: new ToArrayVisitor(),
        }),
      ).toThrow('Not supported: object = MyControl');
    });
  });

  describe('with partial visitor', () => {
    class PartialVisitor implements Visitor {}

    it('still works for bottom-up', () => {
      expect(() =>
        new FormWalker().walkDepthFirst({
          root: group,
          descentStrategy: 'bottom-up',
          visitor: new PartialVisitor(),
        }),
      ).not.toThrow();
    });

    it('still works for top-down', () => {
      expect(() =>
        new FormWalker().walkDepthFirst({
          root: group,
          descentStrategy: 'top-down',
          visitor: new PartialVisitor(),
        }),
      ).not.toThrow();
    });
  });
});
