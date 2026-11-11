import { createElement } from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { describe, expect, it } from 'vitest';
import { TestScreen } from './TestScreen';

describe('práctica anterior al tiempo oficial', () => {
  it('presenta un ejercicio de selección con alternativas sin iniciar el reloj', () => {
    const html = renderToStaticMarkup(
      createElement(TestScreen, {
        testName: 'Ejemplo ficticio',
        questionNumber: 1,
        totalQuestions: 1,
        timeRemainingSeconds: null,
        phase: 'practice',
        prompt: 'Elija una alternativa de ejemplo',
        options: [{ id: 'A', label: 'Respuesta de práctica' }],
        selectedOption: null,
        saveState: 'saved',
        onNext: () => undefined,
      }),
    );
    expect(html).toContain('Elija una alternativa de ejemplo');
    expect(html).toContain('Respuesta de práctica');
    expect(html).toContain('type="radio"');
    expect(html).not.toContain('dateTime="PT');
  });

  it('presenta un campo editable de práctica de texto', () => {
    const html = renderToStaticMarkup(
      createElement(TestScreen, {
        testName: 'FV',
        questionNumber: 1,
        totalQuestions: 1,
        timeRemainingSeconds: null,
        phase: 'practice',
        prompt: 'Escriba una palabra de ejemplo',
        responseType: 'text',
        textResponse: 'mesa',
        selectedOption: null,
        saveState: 'saved',
        onNext: () => undefined,
      }),
    );
    expect(html).toContain('mesa');
    expect(html).toContain('textarea');
  });
});
