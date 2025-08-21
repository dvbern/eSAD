export interface DirtyMarker {
  /**
   * @returns `true` if the component is dirty and the user should be prompted before navigating away.
   */
  isDirty(): boolean | Promise<boolean>;
}

export function isDirtyMarker(component: unknown): component is DirtyMarker {
  return typeof (component as DirtyMarker)?.isDirty === 'function';
}

