// ============================================================
// app.js — Job Application Tracker
// All API calls via fetch() — no frameworks
// ============================================================

const BASE_URL = 'http://localhost:8080';

// ── DOM Elements ─────────────────────────────────────────────
const form          = document.getElementById('applicationForm');
const tableBody     = document.getElementById('tableBody');
const filterStatus  = document.getElementById('filterStatus');
const emptyState    = document.getElementById('emptyState');
const modal         = document.getElementById('modal');
const modalTitle    = document.getElementById('modalTitle');
const modalContent  = document.getElementById('modalContent');
const modalClose    = document.getElementById('modalClose');
const themeToggle   = document.getElementById('themeToggle');
const themeIcon     = document.getElementById('themeIcon');

// ── Theme Toggle ──────────────────────────────────────────────
function initTheme() {
  const saved = localStorage.getItem('theme');
  if (saved === 'dark') {
    document.body.classList.add('dark');
    themeIcon.textContent = '☀️';
  }
}

themeToggle.addEventListener('click', () => {
  document.body.classList.toggle('dark');
  const isDark = document.body.classList.contains('dark');
  themeIcon.textContent = isDark ? '☀️' : '🌙';
  localStorage.setItem('theme', isDark ? 'dark' : 'light');
});

// ── Load Applications ─────────────────────────────────────────
async function loadApplications(status = '') {
  try {
    const url = status
      ? `${BASE_URL}/api/applications?status=${status}`
      : `${BASE_URL}/api/applications`;

    const res  = await fetch(url);
    const data = await res.json();
    renderTable(data);
  } catch (err) {
    console.error('Failed to load applications:', err);
    showError('Could not connect to server. Make sure Spring Boot is running on port 8080.');
  }
}

// ── Render Table ──────────────────────────────────────────────
function renderTable(applications) {
  tableBody.innerHTML = '';

  if (!applications || applications.length === 0) {
    emptyState.style.display = 'block';
    return;
  }

  emptyState.style.display = 'none';

  applications.forEach(app => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${escapeHtml(app.companyName)}</strong></td>
      <td>${escapeHtml(app.jobRole)}</td>
      <td>${formatDate(app.dateApplied)}</td>
      <td><span class="badge badge-${app.status}">${capitalise(app.status)}</span></td>
      <td>${app.notes ? escapeHtml(app.notes) : '<span style="color:var(--text-secondary)">—</span>'}</td>
      <td class="actions-cell">
        <button class="btn btn-prepare" onclick="prepareInterview(${app.id}, '${escapeHtml(app.companyName)}', '${escapeHtml(app.jobRole)}')">🤖 Prepare</button>
        <button class="btn btn-danger"  onclick="deleteApplication(${app.id})">🗑 Delete</button>
      </td>
    `;
    tableBody.appendChild(tr);
  });
}

// ── Add Application ───────────────────────────────────────────
form.addEventListener('submit', async (e) => {
  e.preventDefault();

  const companyName = document.getElementById('companyName').value.trim();
  const jobRole     = document.getElementById('jobRole').value.trim();
  const dateApplied = document.getElementById('dateApplied').value;
  const status      = document.getElementById('status').value;
  const notes       = document.getElementById('notes').value.trim();

  if (!companyName || !jobRole || !dateApplied) {
    alert('Please fill in Company Name, Job Role, and Date Applied.');
    return;
  }

  try {
    const res = await fetch(`${BASE_URL}/api/applications`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ companyName, jobRole, dateApplied, status, notes })
    });

    if (res.status === 201 || res.ok) {
      form.reset();
      loadApplications(filterStatus.value);
    } else {
      const err = await res.json();
      alert('Error: ' + (err.message || 'Could not add application.'));
    }
  } catch (err) {
    console.error('Failed to add application:', err);
    alert('Could not connect to server. Make sure Spring Boot is running.');
  }
});

// ── Delete Application ────────────────────────────────────────
async function deleteApplication(id) {
  if (!confirm('Delete this application?')) return;

  try {
    const res = await fetch(`${BASE_URL}/api/applications/${id}`, {
      method: 'DELETE'
    });

    if (res.status === 204 || res.ok) {
      loadApplications(filterStatus.value);
    } else {
      alert('Could not delete application.');
    }
  } catch (err) {
    console.error('Failed to delete:', err);
    alert('Could not connect to server.');
  }
}

// ── Prepare for Interview (Gemini AI) ─────────────────────────
async function prepareInterview(id, company, role) {
  modalTitle.textContent  = `Interview Prep — ${company}`;
  modalContent.innerHTML  = '<p class="loading-text">🤖 Generating questions with AI...</p>';
  modal.style.display     = 'flex';

  try {
    const res  = await fetch(`${BASE_URL}/api/applications/${id}/prepare`, {
      method: 'POST'
    });
    let data = null;

    try {
      data = await res.json();
    } catch (parseError) {
      data = null;
    }

    if (!res.ok) {
      const message = data && (data.error || data.message)
        ? (data.error || data.message)
        : 'Failed to generate questions.';
      throw new Error(message);
    }

    // data is a list of InterviewQuestion objects — each has a "questionText" field
    if (data && data.length > 0) {
      modalContent.innerHTML = data.map((q, i) => `
        <div class="question-item">
        <strong>Q${i + 1}.</strong> ${escapeHtml(q.questionText || '')}
        </div>
`).join('');
    } else {
      modalContent.innerHTML = '<p style="color:var(--text-secondary);text-align:center;">Could not generate questions right now. Please try again later.</p>';
    }
  } catch (err) {
    console.error('Failed to get questions:', err);
    modalContent.innerHTML = '<p style="color:var(--text-secondary);text-align:center;">Could not generate questions right now. Please try again later.</p>';
  }
}

// ── Filter ────────────────────────────────────────────────────
filterStatus.addEventListener('change', () => {
  loadApplications(filterStatus.value);
});

// ── Modal Close ───────────────────────────────────────────────
modalClose.addEventListener('click', () => {
  modal.style.display = 'none';
});

modal.addEventListener('click', (e) => {
  if (e.target === modal) modal.style.display = 'none';
});

document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') modal.style.display = 'none';
});

// ── Helpers ───────────────────────────────────────────────────
function escapeHtml(str) {
  if (!str) return '';
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function capitalise(str) {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

function showError(msg) {
  tableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:#ef4444;padding:2rem;">${msg}</td></tr>`;
  emptyState.style.display = 'none';
}

// ── Init ──────────────────────────────────────────────────────
initTheme();
loadApplications();
