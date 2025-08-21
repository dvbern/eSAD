import {className} from './className';

describe('className', () => {
  it.each([
    [
      new class Foo {
      }, 'Foo',
    ],
    [123, 'Number'],
    [new Date(), 'Date'],
    [{}, 'Object'],
    [[], 'Array'],
    [() => 123, 'Function'],
    [null, undefined],
    [undefined, undefined],
  ])('returns the class name of an object: %p => %p', (input, expectedName) => {
    expect(className(input))
      .toBe(expectedName);
  });

});
