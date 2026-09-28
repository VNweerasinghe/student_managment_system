const storageKey = 'student-management.records.v1';
const sampleStudents = [
  { id: 'sample-1', studentNumber: 'STU-1001', fullName: 'Ava Patel', email: 'ava.patel@example.com', phone: '555-0101', course: 'Computer Science', status: 'Enrolled' },
  { id: 'sample-2', studentNumber: 'STU-1002', fullName: 'Noah Kim', email: 'noah.kim@example.com', phone: '555-0102', course: 'Business', status: 'Enrolled' },
  { id: 'sample-3', studentNumber: 'STU-1003', fullName: 'Mia Garcia', email: 'mia.garcia@example.com', phone: '555-0103', course: 'Data Science', status: 'Graduated' }
];

const rows = document.querySelector('#student-rows');
const emptyState = document.querySelector('#empty-state');
const searchInput = document.querySelector('#search-input');
const statusFilter = document.querySelector('#status-filter');
const dialog = document.querySelector('#student-dialog');
const form = document.querySelector('#student-form');
const toast = document.querySelector('#toast');
let students = loadStudents();
let toastTimer;

function loadStudents() {
  try {
    const saved = localStorage.getItem(storageKey);
    if (saved === null) {
      localStorage.setItem(storageKey, JSON.stringify(sampleStudents));
      return [...sampleStudents];
    }
    const parsed = JSON.parse(saved);
    return Array.isArray(parsed) ? parsed : [...sampleStudents];
  } catch {
    return [...sampleStudents];
  }
}

function saveStudents() {
  try {
    localStorage.setItem(storageKey, JSON.stringify(students));
    return true;
  } catch {
    showToast('Could not save changes in this browser.');
    return false;
  }
}

function visibleStudents() {
  const query = searchInput.value.trim().toLocaleLowerCase();
  const status = statusFilter.value;
  return students
    .filter((student) => status === 'All' || student.status === status)
    .filter((student) => [student.studentNumber, student.fullName, student.email, student.phone, student.course, student.status]
      .some((value) => value.toLocaleLowerCase().includes(query)))
    .sort((left, right) => left.fullName.localeCompare(right.fullName));
}

function makeCell(className = '') {
  const cell = document.createElement('td');
  if (className) cell.className = className;
  return cell;
}

function renderStudents() {
  const filtered = visibleStudents();
  rows.replaceChildren();
  emptyState.hidden = filtered.length !== 0;
  document.querySelector('#result-count').textContent = `${filtered.length} ${filtered.length === 1 ? 'record' : 'records'}`;
  document.querySelector('#total-count').textContent = students.length;
  document.querySelector('#enrolled-count').textContent = students.filter((student) => student.status === 'Enrolled').length;
  document.querySelector('#graduated-count').textContent = students.filter((student) => student.status === 'Graduated').length;

  for (const student of filtered) {
    const row = document.createElement('tr');
    const identity = makeCell();
    const name = document.createElement('span');
    name.className = 'student-name';
    name.textContent = student.fullName;
    const number = document.createElement('span');
    number.className = 'student-id';
    number.textContent = student.studentNumber;
    identity.append(name, number);

    const contact = makeCell();
    const email = document.createElement('span');
    email.className = 'student-email';
    email.textContent = student.email;
    const phone = document.createElement('span');
    phone.className = 'student-phone';
    phone.textContent = student.phone;
    contact.append(email, phone);

    const course = makeCell();
    course.textContent = student.course;
    const statusCell = makeCell();
    const badge = document.createElement('span');
    badge.className = `status-pill status-${student.status.toLowerCase().replaceAll(' ', '-')}`;
    badge.textContent = student.status;
    statusCell.append(badge);

    const actions = makeCell();
    const actionGroup = document.createElement('span');
    actionGroup.className = 'row-actions';
    actionGroup.append(createAction('Edit', 'edit', student.id), createAction('Delete', 'delete', student.id));
    actions.append(actionGroup);
    row.append(identity, contact, course, statusCell, actions);
    rows.append(row);
  }
}

function createAction(label, action, id) {
  const button = document.createElement('button');
  button.className = `row-action${action === 'delete' ? ' delete' : ''}`;
  button.type = 'button';
  button.dataset.action = action;
  button.dataset.id = id;
  button.textContent = label;
  return button;
}

function openDialog(student) {
  form.reset();
  document.querySelector('#form-error').hidden = true;
  document.querySelector('#record-id').value = student?.id ?? '';
  document.querySelector('#dialog-title').textContent = student ? 'Edit student' : 'Add student';
  form.elements.studentNumber.value = student?.studentNumber ?? '';
  form.elements.fullName.value = student?.fullName ?? '';
  form.elements.email.value = student?.email ?? '';
  form.elements.phone.value = student?.phone ?? '';
  form.elements.course.value = student?.course ?? '';
  form.elements.status.value = student?.status ?? 'Enrolled';
  dialog.showModal();
  form.elements.studentNumber.focus();
}

function showToast(message) {
  toast.textContent = message;
  toast.classList.add('visible');
  window.clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => toast.classList.remove('visible'), 2600);
}

document.querySelector('#add-student-button').addEventListener('click', () => openDialog(null));
document.querySelector('#close-dialog-button').addEventListener('click', () => dialog.close());
document.querySelector('#cancel-dialog-button').addEventListener('click', () => dialog.close());
searchInput.addEventListener('input', renderStudents);
statusFilter.addEventListener('change', renderStudents);

rows.addEventListener('click', (event) => {
  const button = event.target.closest('button[data-action]');
  if (!button) return;
  const student = students.find((record) => record.id === button.dataset.id);
  if (!student) return;
  if (button.dataset.action === 'edit') {
    openDialog(student);
    return;
  }
  if (!window.confirm(`Delete ${student.fullName}? This cannot be undone.`)) return;
  const previous = students;
  students = students.filter((record) => record.id !== student.id);
  if (saveStudents()) {
    renderStudents();
    showToast('Student record deleted.');
  } else {
    students = previous;
  }
});

form.addEventListener('submit', (event) => {
  event.preventDefault();
  const recordId = document.querySelector('#record-id').value;
  const values = Object.fromEntries(new FormData(form));
  const duplicate = students.some((student) => student.studentNumber.trim().toLocaleLowerCase() === values.studentNumber.trim().toLocaleLowerCase() && student.id !== recordId);
  const error = document.querySelector('#form-error');
  if (duplicate) {
    error.textContent = 'That student ID is already in use.';
    error.hidden = false;
    form.elements.studentNumber.focus();
    return;
  }

  const previous = students;
  const student = {
    id: recordId || (crypto.randomUUID?.() ?? `student-${Date.now()}-${Math.random().toString(16).slice(2)}`),
    studentNumber: values.studentNumber.trim(),
    fullName: values.fullName.trim(),
    email: values.email.trim(),
    phone: values.phone.trim(),
    course: values.course.trim(),
    status: values.status
  };
  students = recordId
    ? students.map((current) => current.id === recordId ? student : current)
    : [...students, student];
  if (saveStudents()) {
    renderStudents();
    dialog.close();
    showToast(recordId ? 'Student details updated.' : 'Student added.');
  } else {
    students = previous;
  }
});

document.querySelector('#export-button').addEventListener('click', () => {
  const values = [
    ['Student ID', 'Full name', 'Email', 'Phone', 'Course', 'Status'],
    ...visibleStudents().map((student) => [student.studentNumber, student.fullName, student.email, student.phone, student.course, student.status])
  ];
  const csv = values.map((row) => row.map((value) => `"${String(value).replaceAll('"', '""')}"`).join(',')).join('\r\n');
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
  const link = document.createElement('a');
  link.href = url;
  link.download = 'students.csv';
  link.click();
  URL.revokeObjectURL(url);
  showToast(`Exported ${values.length - 1} student ${values.length === 2 ? 'record' : 'records'}.`);
});

renderStudents();