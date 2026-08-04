/* eslint-disable @typescript-eslint/no-explicit-any */
import {hasSomeNotNullishValueIgnoringVersion} from './untersuchung.util';

describe('untersuchung.util', () => {
  describe('hasSomeNotNullishValueIgnoringVersion', () => {
    it.each([
      {input: {version: 0}, expected: false, description: 'only version field'},
      {input: {version: 1, value: null}, expected: false, description: 'null value'},
      {input: {version: 0, value: undefined}, expected: false, description: 'undefined value'},
      {input: {version: 0, value: ''}, expected: false, description: 'empty string'},
      {input: {version: 0, value: '   '}, expected: false, description: 'whitespace-only string'},
      {input: {version: 0, value: []}, expected: false, description: 'empty array'},
      {input: {version: 0, value: {}}, expected: false, description: 'empty object'},
      {input: {version: 0, nested: {inner: null}}, expected: false, description: 'nested null'},
      {input: {version: 0, nested: {inner: undefined}}, expected: false, description: 'nested undefined'},
      {input: {version: 0, arr: [null, undefined]}, expected: false, description: 'array of nullish values'},
      {input: {version: 0, value: 'hello'}, expected: true, description: 'non-empty string'},
      {input: {version: 0, value: 42}, expected: true, description: 'number'},
      {input: {version: 0, value: 0}, expected: true, description: 'zero (falsy but not nullish)'},
      {input: {version: 0, value: false}, expected: true, description: 'false (falsy but not nullish)'},
      {input: {version: 0, value: true}, expected: true, description: 'true'},
      {input: {version: 0, nested: {inner: 'value'}}, expected: true, description: 'nested non-empty string'},
      {input: {version: 0, nested: {deep: {value: 7}}}, expected: true, description: 'deeply nested number'},
      {input: {version: 0, arr: [null, 'found']}, expected: true, description: 'array with one non-nullish value'},
    ])('should return $expected for $description', ({input, expected}) => {
      expect(hasSomeNotNullishValueIgnoringVersion(input)).toBe(expected);
    });

    describe('nested version keys', () => {
      it('should strip version at every level, not just the top', () => {
        const input: any = {version: 0, nested: {version: 0}};

        expect(hasSomeNotNullishValueIgnoringVersion(input)).toBe(false);
      });

      it('should find real content alongside a nested version key', () => {
        const input: any = {version: 5, nested: {version: 3, value: 'x'}};

        expect(hasSomeNotNullishValueIgnoringVersion(input)).toBe(true);
      });
    });

    describe('cyclic and shared references', () => {
      it('should terminate and return false for a self-referencing object with no content', () => {
        const node: any = {version: 0};
        node.self = node;

        expect(hasSomeNotNullishValueIgnoringVersion(node)).toBe(false);
      });

      it('should terminate and return true when a self-referencing object also has content', () => {
        const node: any = {version: 0, value: 'found'};
        node.self = node;

        expect(hasSomeNotNullishValueIgnoringVersion(node)).toBe(true);
      });

      it('should terminate and return false for a bidirectional relation between two empty objects', () => {
        const a: any = {version: 0};
        const b: any = {};
        a.b = b;
        b.a = a;

        expect(hasSomeNotNullishValueIgnoringVersion(a)).toBe(false);
      });

      it('should find content past a child loop', () => {
        const a: any = {version: 0, foo: undefined};
        const b: any = {child: undefined, label: 'hi'};
        const c: any = {parent: undefined};
        b.child = c;
        c.parent = b;
        a.foo = b;

        expect(hasSomeNotNullishValueIgnoringVersion(a)).toBe(true);
      });

      it('should terminate and return false for a child loop with no content', () => {
        const a: any = {version: 0, foo: undefined};
        const b: any = {child: undefined};
        const c: any = {parent: undefined};
        b.child = c;
        c.parent = b;
        a.foo = b;

        expect(hasSomeNotNullishValueIgnoringVersion(a)).toBe(false);
      });

      it('should find content across a bidirectional relation', () => {
        const a: any = {version: 0};
        const b: any = {value: 'deep'};
        a.b = b;
        b.a = a;

        expect(hasSomeNotNullishValueIgnoringVersion(a)).toBe(true);
      });

      it('should terminate for a cycle through an array', () => {
        const arr: any[] = [];
        const node: any = {version: 0, arr};
        arr.push(node);

        expect(hasSomeNotNullishValueIgnoringVersion(node)).toBe(false);
      });

      it('should handle the same empty object shared under two keys', () => {
        const shared = {};
        const input: any = {version: 0, first: shared, second: shared};

        expect(hasSomeNotNullishValueIgnoringVersion(input)).toBe(false);
      });

      it('should handle the same content-bearing object shared under two keys', () => {
        const shared = {value: 'x'};
        const input: any = {version: 0, first: shared, second: shared};

        expect(hasSomeNotNullishValueIgnoringVersion(input)).toBe(true);
      });
    });
  });
});
