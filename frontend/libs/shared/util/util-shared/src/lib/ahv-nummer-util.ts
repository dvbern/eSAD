export class AhvNummerUtil {
  // AhvNummerUtil copied from VacMe

  public static parseAhvNummer(ahvNummer: string): number {
    return parseInt(ahvNummer.replace(/\./g, ''), 10);
  }

  public static validateAhvNummer(ahvNummer: number): boolean {
    return ahvNummer % 10 === AhvNummerUtil.getPruefziffer(ahvNummer);
  }

  private static getPruefziffer(ahvNummer: number): number {
    let n = Math.floor(ahvNummer / 10);
    let pruefziffer = 0;
    let z = 1;
    while (n > 0) {
      const ziffer = n % 10;
      pruefziffer = AhvNummerUtil.incrementPruefziffer(pruefziffer, ziffer, z);
      z++;
      n = Math.floor(n / 10);
    }
    let returnValue = 0;
    if (pruefziffer % 10 !== 0) {
      returnValue = 10 - pruefziffer % 10;
    }
    return returnValue;
  }

  private static incrementPruefziffer(pruefziffer: number, ziffer: number, z: number): number {
    let increment = ziffer;
    if (z % 2 !== 0) {
      increment *= 3;
    }
    return pruefziffer + increment;
  }
}
