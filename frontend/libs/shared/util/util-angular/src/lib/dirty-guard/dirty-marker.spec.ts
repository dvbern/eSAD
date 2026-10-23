import {ensureProps} from '@dvb/util-shared';
import {DirtyMarker, isDirtyMarker} from './dirty-marker';

describe('DirtyMarker', () => {

  describe('isDirtyMarker', () => {
    it('returns true if the components implements the interface', () => {
      const impl = ensureProps<DirtyMarker>({
        isDirty: vi.fn(),
      });

      const actual = isDirtyMarker(impl);

      expect(actual)
        .toBe(true);
    })

    it.each([
      undefined,
      null,
      {},
      {dirtyMarker: 123},
      {foo: () => vi.fn()},
    ])('returns false if the component does *not* implement the interface: %s', (input) => {
      const actual = isDirtyMarker(input);

      expect(actual)
        .toBe(false);
    })
  });
});
