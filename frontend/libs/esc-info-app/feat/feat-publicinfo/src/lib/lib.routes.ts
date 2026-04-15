import { Route } from '@angular/router';
import { PublicinfoPageComponent } from './publicinfo/publicinfo-page.component';

import { QuizPageComponent } from './quiz/quiz-page.component';

export const publicinfoRoutes: Route[] = [
  {
    path: '', component: PublicinfoPageComponent
  },
  {
    path: 'quiz', component: QuizPageComponent
  }
];
