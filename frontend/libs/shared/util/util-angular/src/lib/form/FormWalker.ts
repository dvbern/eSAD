import {AbstractControl, FormArray, FormControl, FormGroup} from '@angular/forms';
import {className} from '@dvb/util-shared';

export interface VisitorContext<Ctl extends AbstractControl> {
  name: string;
  path: string;
  control: Ctl;
  parent: AbstractControl | null;
}

export interface Visitor {
  onAnyControl?(ctx: VisitorContext<FormControl> | VisitorContext<FormGroup> | VisitorContext<FormArray>): void;

  onFormGroup?(ctx: VisitorContext<FormGroup>): void;

  onFormArray?(ctx: VisitorContext<FormArray>): void;

  onFormControl?(ctx: VisitorContext<FormControl<unknown>>): void;
}

export type DescentStrategy = 'top-down' | 'bottom-up';

export type FormWalkerParams<T extends Visitor> = {
  root: AbstractControl,
  descentStrategy: DescentStrategy;
  visitor: T;
}

export class FormWalker {
  walkDepthFirst<T extends Visitor>(
    params: FormWalkerParams<T>,
  ): T {
    const ctx: VisitorContext<AbstractControl> = {
      name: '',
      path: '',
      control: params.root,
      parent: null,
    };

    this.visitImpl(params, ctx);

    return params.visitor;
  }

  private visitImpl(params: FormWalkerParams<Visitor>, ctx: VisitorContext<AbstractControl>): void {
    if (ctx.control instanceof FormGroup) {
      if (params.descentStrategy === 'top-down') {
        params.visitor.onAnyControl?.(ctx as VisitorContext<FormGroup>);
        params.visitor.onFormGroup?.(ctx as VisitorContext<FormGroup>);
      }
      this.visitFormGroup(params, ctx as VisitorContext<FormGroup>);
      if (params.descentStrategy === 'bottom-up') {
        params.visitor.onAnyControl?.(ctx as VisitorContext<FormGroup>);
        params.visitor.onFormGroup?.(ctx as VisitorContext<FormGroup>);
      }
    } else if (ctx.control instanceof FormArray) {
      if (params.descentStrategy === 'top-down') {
        params.visitor.onAnyControl?.(ctx as VisitorContext<FormArray>);
        params.visitor.onFormArray?.(ctx as VisitorContext<FormArray>);
      }
      this.visitFormArray(params, ctx as VisitorContext<FormArray>);
      if (params.descentStrategy === 'bottom-up') {
        params.visitor.onAnyControl?.(ctx as VisitorContext<FormArray>);
        params.visitor.onFormArray?.(ctx as VisitorContext<FormArray>);
      }
    } else if (ctx.control instanceof FormControl) {
      params.visitor.onAnyControl?.(ctx as VisitorContext<FormControl<unknown>>);
      params.visitor.onFormControl?.(ctx as VisitorContext<FormControl<unknown>>);
    } else {
      throw Error(`Not supported: ${typeof ctx.control} = ${className(ctx.control)}`);
    }
  }

  private visitFormGroup(params: FormWalkerParams<Visitor>, ctx: VisitorContext<FormGroup>): void {
    Object.keys(ctx.control.controls).forEach((key) => {
      const control: AbstractControl = ctx.control.controls[key];
      this.visitImpl(
        params,
        {
          name: key,
          control,
          parent: control,
          path: `${ctx.path}${ctx.path === '' ? '' : '.'}${key}`,
        });
    });
  }

  private visitFormArray(params: FormWalkerParams<Visitor>, ctx: VisitorContext<FormArray>): void {
    for (let i = 0; i < Object.keys(ctx.control.controls).length; i++) {
      const control: AbstractControl = ctx.control.controls[i];

      this.visitImpl(
        params,
        {
          name: `${i}`,
          control,
          parent: control,
          path: `${ctx.path}[${i}]`,
        });
    }
  }
}
