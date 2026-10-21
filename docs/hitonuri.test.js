const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');
const vm = require('node:vm');

class FakeElement {
  constructor(id = '') {
    this.id = id;
    this.children = [];
    this.className = '';
    this.style = {};
    this.disabled = false;
    this.textContent = '';
    this.onclick = null;
    this.listeners = {};
    this.classList = {
      values: new Set(),
      add: (name) => this.classList.values.add(name),
      remove: (name) => this.classList.values.delete(name),
      contains: (name) => this.classList.values.has(name)
    };
  }

  set innerHTML(value) {
    this.children = [];
    this._innerHTML = value;
  }

  get innerHTML() { return this._innerHTML || ''; }
  append(element) { this.children.push(element); }

  addEventListener(type, listener) {
    this.listeners[type] = this.listeners[type] || [];
    this.listeners[type].push(listener);
  }

  click() {
    if (this.disabled) return;
    if (this.onclick) this.onclick({ target: this });
    (this.listeners.click || []).forEach((listener) => listener({ target: this }));
  }
}

function loadGame() {
  const ids = [
    'board', 'title', 'clock', 'message', 'switch', 'restart', 'restart2', 'undo',
    'next', 'next2', 'prev', 'solve', 'helpBtn', 'helpBtn2', 'legacy', 'closeLegacy'
  ];
  const elements = Object.fromEntries(ids.map((id) => [id, new FakeElement(id)]));
  const document = {
    getElementById: (id) => elements[id],
    createElement: () => new FakeElement(),
    addEventListener: (type, listener) => { document.listeners[type] = listener; },
    listeners: {}
  };
  const source = fs.readFileSync(path.join(__dirname, 'hitonuri.js'), 'utf8');
  vm.runInNewContext(source, { document, Date, setInterval: () => {} });
  return { document, elements };
}

test('renders the first Hitori puzzle from the Palm catalogue', () => {
  const { elements } = loadGame();
  assert.equal(elements.title.textContent, 'Hitori 1');
  assert.equal(elements.board.children.length, 25);
  assert.equal(elements.board.children[0].textContent, '1');
  assert.match(elements.message.textContent, /black it out/);
});

test('switches to Nurikabe and cycles an unnumbered cell', () => {
  const { elements } = loadGame();
  elements.switch.click();
  assert.equal(elements.title.textContent, 'Nurikabe 1');
  assert.equal(elements.board.children.length, 25);

  const emptyIndex = elements.board.children.findIndex((cell) => !cell.disabled);
  elements.board.children[emptyIndex].click();
  assert.match(elements.board.children[emptyIndex].className, /dot/);
  // The grid is redrawn on every tap, so retrieve the same cell again.
  elements.board.children[emptyIndex].click();
  assert.match(elements.board.children[emptyIndex].className, /black/);
});

test('shows a solution and reports completion', () => {
  const { elements } = loadGame();
  elements.solve.click();
  assert.equal(elements.message.textContent, ':) Solved!');
  assert.equal(elements.message.className, 'message win');
});

test('undo restores the previous cell state and disables when history is empty', () => {
  const { elements } = loadGame();
  const originalClass = elements.board.children[0].className;
  assert.equal(elements.undo.disabled, true);

  elements.board.children[0].click();
  assert.notEqual(elements.board.children[0].className, originalClass);
  assert.equal(elements.undo.disabled, false);

  elements.undo.click();
  assert.equal(elements.board.children[0].className, originalClass);
  assert.equal(elements.undo.disabled, true);
});

test('the question-mark control opens and closes the legacy page', () => {
  const { elements } = loadGame();
  elements.helpBtn.click();
  assert.equal(elements.legacy.classList.contains('open'), true);
  elements.closeLegacy.click();
  assert.equal(elements.legacy.classList.contains('open'), false);
});

test('next and previous wrap through the bundled Hitori puzzles', () => {
  const { elements } = loadGame();
  for (let index = 0; index < 7; index += 1) elements.next.click();
  assert.equal(elements.title.textContent, 'Hitori 1');
  elements.prev.click();
  assert.equal(elements.title.textContent, 'Hitori 7');
});
