export class SortUtil {
  static usingPreferredValueFirst<T>(value: T): (a: T, b: T) => number {
    return (a: T, b: T) => (a === value ? -1 : b === value ? 1 : 0);
  }

  /**
   * Hauptsaechlich dazu gedacht, um uebersetzte Werte zu sortieren.
   *
   * Transformiert und sortiert danach mit {@link SortUtil.localeCompareStr}.
   */
  static usingTransformedValue<T>(
    transform: (value: T) => string,
    locale: string,
  ): (a: T, b: T) => number {
    return (a: T, b: T) =>
      this.localeCompareStr(transform(a), transform(b), locale);
  }

  /**
   * Behandelt den Vergleich von NULL-Werten und Strings.
   * Bei anderen Typen wird ein Vergleich mit grösser/kleiner gleich durchgeführt.
   */
  static handleCompareStandard(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    value1: any,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    value2: any,
    locale: string,
  ): number {
    // undefined wird bei der Sortierung wie null behandelt
    const value1Null = value1 === null || value1 === undefined;
    const value2Null = value2 === null || value2 === undefined;

    if (value1Null && !value2Null) {
      return -1;
    }

    if (!value1Null && value2Null) {
      return 1;
    }

    if (value1Null && value2Null) {
      return 0;
    }

    if (typeof value1 === 'string' && typeof value2 === 'string') {
      return this.localeCompareStr(value1, value2, locale);
    }

    return value1 < value2 ? -1 : value1 > value2 ? 1 : 0;
  }

  /**
   * Vergleicht zwei Strings.
   *
   * @param str1
   * @param str2
   */
  static localeCompareStr(str1: string, str2: string, locale: string): number {
    return str1.localeCompare(str2, locale, {sensitivity: 'base'});
  }
}
