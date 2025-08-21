export function className(obj: unknown): string | undefined {
  return obj?.constructor?.name;
}
