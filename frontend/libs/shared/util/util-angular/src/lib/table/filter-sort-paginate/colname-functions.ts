import { isNotNullish, Leaves } from '@dvb/util-shared';

export const getByPath = <T extends object>(obj: T, column: Leaves<T>): unknown => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  let result: any = obj;
  const strings = column.split('.');
  for (const col of strings) {
    if (isNotNullish(result)) {
      result = result[col];
    }
  }
  return result;
};
