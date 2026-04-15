import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PageComponent, TenantSettingsStore } from '@dvb/feat-shared';
import { TenantEnum } from '@dvb/data-access-backend';
import { RadioDirective, SingleRadioWrapperComponent } from '@dvb/ui-components';

interface QuizData {
  question: string;
  options: QuizOption[];
  funFact: string;
}

interface QuizOption {
  label: string;
  isCorrect: boolean;
}

@Component({
  selector: 'feat-quiz-page',
  standalone: true,
  imports: [PageComponent, FormsModule, RadioDirective, SingleRadioWrapperComponent],
  templateUrl: './quiz-page.component.html',
  styleUrl: './quiz-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuizPageComponent {
  private readonly tenantSettingsStore = inject(TenantSettingsStore);

  readonly quizzes: Record<string, QuizData> = {
    [TenantEnum.BS]: {
      question: 'Which Swiss canton is famous for its "Basler Läckerli" and its world-renowned art fair?',
      options: [
        { label: 'Bern', isCorrect: false },
        { label: 'Basel-Stadt', isCorrect: true },
        { label: 'Zürich', isCorrect: false },
        { label: 'Geneva', isCorrect: false },
      ],
      funFact: 'Fun Fact: Basel has the highest concentration of museums in Switzerland, with nearly 40 in total!',
    },
    [TenantEnum.BE]: {
      question: 'Which famous scientist developed the Theory of Relativity while working as a patent clerk in Bern?',
      options: [
        { label: 'Isaac Newton', isCorrect: false },
        { label: 'Marie Curie', isCorrect: false },
        { label: 'Albert Einstein', isCorrect: true },
        { label: 'Nikola Tesla', isCorrect: false },
      ],
      funFact: 'Fun Fact: The Einstein House in Bern\'s Old Town is where he lived from 1903 to 1905, his "Annus Mirabilis".',
    },
    [TenantEnum.WIN]: {
      question: 'Winterthur is known as the "City of Museums", but which of these is its largest and most famous art collection?',
      options: [
        { label: 'Kunsthaus Zürich', isCorrect: false },
        { label: 'Museum Oskar Reinhart', isCorrect: true },
        { label: 'Technorama', isCorrect: false },
        { label: 'Fotomuseum', isCorrect: false },
      ],
      funFact: 'Fun Fact: Winterthur was once one of the most important industrial centers in Switzerland, home to Sulzer and Rieter.',
    },
    ['DEFAULT']: {
      question: 'What is the capital of Switzerland?',
      options: [
        { label: 'Zürich', isCorrect: false },
        { label: 'Geneva', isCorrect: false },
        { label: 'Bern', isCorrect: true },
        { label: 'Basel', isCorrect: false },
      ],
      funFact: 'Fun Fact: Bern is not technically the capital, but the "Federal City" (Bundesstadt) of Switzerland.',
    }
  };

  readonly currentQuiz = computed(() => {
    const tenant = this.tenantSettingsStore.settings()?.tenant ?? 'DEFAULT';
    return this.quizzes[tenant] ?? this.quizzes['DEFAULT'];
  });

  selectedOption = signal<QuizOption | null>(null);
  showResult = signal<boolean>(false);

  selectOption(option: QuizOption) {
    if (!this.showResult()) {
      this.selectedOption.set(option);
    }
  }

  onOkPress() {
    if (this.selectedOption()) {
      this.showResult.set(true);
    }
  }

  resetQuiz() {
    this.selectedOption.set(null);
    this.showResult.set(false);
  }
}
