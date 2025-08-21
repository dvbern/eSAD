import { getByPath } from './colname-functions';

interface DummyItem {
  name?: string;
  nummer?: number;
  adresse?: {
    strasse?: string;
    ort?: string;
  };
}

describe('access direct property', () => {
  test('happy case: string', () => {
    const obj: DummyItem = {
      name: 'Robin',
    };
    expect(getByPath(obj, 'name')).toBe('Robin');
  });
  test('happy case: number', () => {
    const obj: DummyItem = {
      nummer: 89,
    };
    expect(getByPath(obj, 'nummer')).toBe(89);
  });
  test('undefined', () => {
    const obj: DummyItem = {};
    expect(getByPath(obj, 'name')).toBe(undefined);
  });
});

describe('access deep property', () => {
  test('happy case', () => {
    const obj: DummyItem = {
      adresse: {
        strasse: 'Meisenweg',
      },
    };
    expect(getByPath(obj, 'adresse.strasse')).toBe('Meisenweg');
  });
  test('undefined adresse', () => {
    const obj: DummyItem = {};
    expect(getByPath(obj, 'adresse.strasse')).toBe(undefined);
  });
  test('defined adresse but undefined strasse', () => {
    const obj: DummyItem = {
      adresse: {
        ort: 'Luzern',
      },
    };
    expect(getByPath(obj, 'adresse.strasse')).toBe(undefined);
  });
});
