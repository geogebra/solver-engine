import {
  jsonToTree,
  MathJson,
  Metadata,
  TaskJson,
  TransformationJson,
  treeToSolver,
} from '@geogebra/solver-sdk';
import { isThroughStep } from './util';
import { settings } from './settings';

export const renderTest = (trans: TransformationJson, methodId: string) => /* HTML */ `
  <details class="test-code hide-in-demo-mode">
    <summary>Test Code <button class="copy-test-code-button">Copy</button></summary>
    <pre>${generateTestSuggestion(trans, methodId)}</pre>
  </details>
`;

export const copyTestCodeToClipboardOnClick = () => {
  const copyToClipboard = async (evt: Event) => {
    const testCode = (evt.target as HTMLElement)?.parentElement?.nextElementSibling?.textContent;
    if (testCode) {
      await navigator.clipboard.writeText(testCode);
    }
  };
  for (const el of document.querySelectorAll<HTMLElement>('.copy-test-code-button')) {
    el.onclick = copyToClipboard;
  }
};

export function generateTestSuggestion(trans: TransformationJson, methodId: string): string {
  const stringBuilder = new StringBuilder();
  new IndentBuilder(stringBuilder).do(buildTest(trans, methodId));
  return stringBuilder.toString();
}

class StringBuilder {
  lines: string[];

  constructor() {
    this.lines = [];
  }

  addLine(line: string) {
    this.lines.push(line);
  }

  toString() {
    return this.lines.join('\n');
  }
}

class IndentBuilder {
  parent: StringBuilder | IndentBuilder;
  indent: string;

  constructor(parent: StringBuilder | IndentBuilder, indent = '') {
    this.parent = parent;
    this.indent = indent;
  }

  child(indent = '    ') {
    return new IndentBuilder(this, indent);
  }

  addLine(line: string) {
    this.parent.addLine(this.indent + line);
    return this;
  }

  do(writeLines: (builder: IndentBuilder) => void) {
    writeLines(this);
  }

  nest(line: string, writeLines: (builder: IndentBuilder) => void, open = ' {', close = '}') {
    this.addLine(line + open);
    writeLines(this.child());
    this.addLine(close);
  }
}

const buildTest = (trans: TransformationJson, methodId: string) => (builder: IndentBuilder) => {
  builder.nest('testMethod', (builder) => {
    builder
      .addLine(`method = ${methodId.replace('.', 'Plans.')}`)
      .addLine(`inputExpr = "${renderExpression(trans.fromExpr)}"`)
      .addLine('')
      .nest('check', buildTestTransformation(trans));
  });
};

const buildTestTransformation = (trans: TransformationJson) => (builder: IndentBuilder) => {
  const throughStep = isThroughStep(trans);
  if (throughStep && !settings.showThroughSteps) {
    builder.do(buildTestTransformation(trans.steps[0]));
    return;
  }
  if (throughStep) {
    builder.addLine('// Through step');
  } else {
    builder
      .addLine(`fromExpr = "${renderExpression(trans.fromExpr)}"`)
      .addLine(`toExpr = "${renderExpression(trans.toExpr)}"`);
    if (trans.explanation) {
      builder.do(buildExplanation(trans.explanation));
    }
  }
  if (trans.steps) {
    for (const step of trans.steps) {
      builder.addLine('').nest('step', buildTestTransformation(step));
    }
  }
  if (trans.tasks) {
    for (const task of trans.tasks) {
      builder.addLine('').nest('task', buildTestTask(task));
    }
  }
};

const buildTestTask = (task: TaskJson) => (builder: IndentBuilder) => {
  builder.addLine(`taskId = "${task.taskId}"`);
  builder.addLine(`startExpr = "${renderExpression(task.startExpr)}"`);
  if (task.explanation) {
    builder.do(buildExplanation(task.explanation));
  }
  if (task.steps) {
    for (const step of task.steps) {
      builder.addLine('').nest('step', buildTestTransformation(step));
    }
  }
};

const buildExplanation = (explanation?: Metadata) => (builder: IndentBuilder) => {
  if (explanation) {
    builder.nest(`explanation`, (builder) => {
      // By convention, the name of the explanation enum in the code is
      // [category]Explanation.[name] given that the key is [category].[name]
      const key = explanation.key.replace('.', 'Explanation.');
      builder.addLine(`key = ${key}`);
    });
  }
};

const renderExpression = (expression: MathJson) =>
  treeToSolver(jsonToTree(expression)).replaceAll('\\', '\\\\');
