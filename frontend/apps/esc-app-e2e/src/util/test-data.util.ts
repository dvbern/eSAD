import {runId} from "./env.util";


export function withTestId(value :string): string{
  return `${value}-${runId}`;
}

export const testData = {
  anlass1 : {
    title: withTestId('Anlass 1'),
  },
  anlass2 : {
    title: withTestId('Anlass 2'),
  },
  anlass3 : {
    title: withTestId('Anlass 3'),
  },
  notizTitle: withTestId('Test Notiz'),
} as const;
