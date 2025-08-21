import {AppFailureErrorJson, AppValidationErrorJson, FailureType, ViolationJson} from '@dvb/data-access-backend';
import {hasValue, isFilledIn, isNullish} from '@dvb/util-shared';

export function isFailureType(value: unknown): value is FailureType {
  return typeof value === 'string'
    && Object.values(FailureType)
      .includes(value as FailureType);
}

export function isAppFailureErrorJson(error: unknown): error is AppFailureErrorJson {
  if (isNullish(error) || typeof error !== 'object') {
    return false;
  }

  const partial = error as Partial<AppFailureErrorJson>;

  const result = isFailureType(partial.type)
    && partial.type === FailureType.FAILURE
    && hasValue(partial.id)
    && hasValue(partial.summary);

  return result;
}

export function isAppValidationErorrJson(error: unknown): error is AppValidationErrorJson {
  if (isNullish(error) || typeof error !== 'object') {
    return false;
  }

  const partial = error as Partial<AppValidationErrorJson>;

  const result = isFailureType(partial.type)
    && partial.type === FailureType.VALIDATION
    && Array.isArray(partial.violations);

  return result;
}

export function makeAppFailureErrorJson(id: string, summary: string): AppFailureErrorJson {
  return {
    type: FailureType.FAILURE,
    id,
    summary,
  };
}

export function isFieldViolation(violation: ViolationJson): boolean {
  // FIXME: is this correct?
  return isFilledIn(violation.path);
}

export function isFormViolation(violation: ViolationJson): boolean {
  // FIXME: is this correct?
  return !isFilledIn(violation.path);
}

