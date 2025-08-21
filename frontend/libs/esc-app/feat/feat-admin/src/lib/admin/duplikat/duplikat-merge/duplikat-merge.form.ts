import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {DuplikatJson, DuplikatKindJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {BaseForm} from '@dvb/util-angular';
import {ensureProps, requireValue} from '@dvb/util-shared';

export interface DuplikatMergeJson {
  duplikatId: BackendEntityId,
  basisKindId: BackendEntityId
}

function makeForm(fb: NonNullableFormBuilder) {
  return fb.group({
    duplikatId: fb.control<BackendEntityId | undefined>(undefined, [Validators.required]),
    basisKind: fb.control<DuplikatKindJson | undefined>(undefined, [Validators.required]),
  });
}

export class DuplikatMergeForm extends BaseForm<ReturnType<typeof makeForm>, DuplikatMergeJson> {
  constructor(fb: NonNullableFormBuilder) {
    super(() => makeForm(fb));
  }

  resetForm(einladung: DuplikatJson): void {
    this.group.reset({
      duplikatId: einladung.id,
      basisKind: undefined,
    });
  }

  valueOnValid(): DuplikatMergeJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<DuplikatMergeJson>({
      duplikatId: requireValue(value.duplikatId),
      basisKindId: requireValue(value.basisKind?.id),
    });
  }
}
