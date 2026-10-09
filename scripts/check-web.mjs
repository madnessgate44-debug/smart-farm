import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import vm from 'node:vm';

const html = readFileSync('index.html', 'utf8');
const scripts = [...html.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/gi)].map(match => match[1]);
assert.equal(scripts.length, 1, 'Expected exactly one inline application script.');
assert.match(html, /البيانات محفوظة في المتصفح ده فقط/);
assert.match(html, /مش متصلة بقاعدة بيانات تطبيق Android/);
assert.doesNotMatch(html, /\sonclick\s*=/i, 'Use delegated event handlers rather than inline click handlers.');
new vm.Script(scripts[0], { filename: 'index.html:inline-script.js' });

function createPage(storageSeed = {}) {
  const storage = new Map(Object.entries(storageSeed));
  const listeners = {};
  const root = {
    innerHTML: '',
    addEventListener(name, handler) { listeners[name] = handler; },
    querySelector() { return null; }
  };
  const status = { textContent: '' };
  const document = {
    getElementById(id) { return id === 'app' ? root : status; },
    createElement() { return { click() {}, remove() {}, set href(value) {}, set download(value) {} }; },
    body: { appendChild() {} }
  };
  class FakeFormData {
    constructor(form) { this.form = form; }
    entries() { return Object.entries(this.form.mockData || {}); }
  }
  const context = {
    document,
    localStorage: {
      getItem(key) { return storage.has(key) ? storage.get(key) : null; },
      setItem(key, value) { storage.set(key, String(value)); },
      removeItem(key) { storage.delete(key); }
    },
    window: {},
    FormData: FakeFormData,
    setTimeout() { return 1; },
    clearTimeout() {},
    console,
    Date,
    Math,
    JSON,
    URL: { createObjectURL() { return ''; }, revokeObjectURL() {} },
    Blob: class {}
  };
  vm.runInNewContext(scripts[0], context, { filename: 'index.html:inline-script.js' });
  return {
    root, status, storage,
    click(action, extra = {}) {
      listeners.click({ target: { dataset: Object.assign({ action }, extra), closest() { return this; } } });
    },
    submit(formName, values) {
      const form = { dataset: { form: formName }, mockData: values, reset() {} };
      listeners.submit({ target: { closest() { return form; } }, preventDefault() {} });
    },
    saved() { return JSON.parse(storage.get('balegh_web_v4')); }
  };
}

// Existing onboarding must not create placeholder workers from an approximate count.
const legacy = {
  balegh_personal_v3: JSON.stringify({
    profile: { name: 'Owner', farmName: 'Farm', complete: true, workers: '3' },
    workers: [
      { code: 'W01', name: 'عامل 1', job: 'عامل' },
      { code: 'W02', name: 'عامل 2', job: 'عامل' },
      { code: 'W03', name: 'عامل 3', job: 'عامل' }
    ],
    inventory: [], tasks: [], finance: [], attendance: [], events: [], memory: [], notes: []
  })
};
const migrated = createPage(legacy);
assert.equal(migrated.saved().workers.length, 0, 'Generated placeholder workers should not be treated as real workers.');

// A new task must not be written until the explicit confirmation action.
const initial = {
  balegh_web_v4: JSON.stringify({
    profile: { name: 'Owner', farmName: 'Farm', complete: true },
    workers: [], inventory: [], movements: [], tasks: [], finance: [], attendance: [], events: [], memory: [], notes: []
  })
};
const page = createPage(initial);
page.submit('task-add', { taskName: 'ري الأرض الغربية', date: '2026-10-09', workerCode: '', notes: '' });
assert.equal(page.saved().tasks.length, 0, 'Task was written before confirmation.');
page.click('confirm-yes');
assert.equal(page.saved().tasks.length, 1, 'Confirmed task was not saved.');
page.click('confirm-yes');
assert.equal(page.saved().tasks.length, 1, 'Repeated confirmation created a duplicate task.');

// An outgoing movement larger than the real balance must not mutate stock.
const stocked = createPage({
  balegh_web_v4: JSON.stringify({
    profile: { name: 'Owner', farmName: 'Farm', complete: true },
    workers: [],
    inventory: [{ code: 'I1', name: 'سماد', unit: 'شكارة', qty: 2, min: 0 }],
    movements: [], tasks: [], finance: [], attendance: [], events: [], memory: [], notes: []
  })
});
stocked.submit('stock-adjust', { code: 'I1', direction: 'out', quantity: '3', reason: 'test' });
assert.equal(stocked.saved().inventory[0].qty, 2, 'Invalid stock issue changed the balance.');
assert.equal(stocked.saved().movements.length, 0, 'Invalid stock issue created a movement.');

console.log('Web checks passed: syntax, local-only disclosure, legacy placeholder cleanup, confirmation gate, duplicate confirmation, and insufficient-stock protection.');
