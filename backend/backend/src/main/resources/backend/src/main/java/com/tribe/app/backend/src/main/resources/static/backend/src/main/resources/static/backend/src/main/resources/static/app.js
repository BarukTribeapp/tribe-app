const state = {
  me: null,
  lessons: [],
  progress: [],
  ranking: [],
  missions: [],
  shop: [],
  activeLesson: null,
  selectedOption: null
};

const statsEl = document.getElementById("stats");
const tabs = document.querySelectorAll(".tab");
const navs = document.querySelectorAll(".nav");

navs.forEach(btn => {
  btn.addEventListener("click", () => {
    navs.forEach(n => n.classList.remove("active"));
    btn.classList.add("active");
    tabs.forEach(t => t.classList.remove("active"));
    document.getElementById(btn.dataset.tab).classList.add("active");
  });
});

async function api(path, options = {}) {
  const res = await fetch(path, {
    headers: { "Content-Type": "application/json" },
    ...options
  });
  return res.json();
}

async function loadAll() {
  const [me, lessons, progress, ranking, missions, shop] = await Promise.all([
    api("/api/me"),
    api("/api/lessons"),
    api("/api/progress"),
    api("/api/ranking"),
    api("/api/missions"),
    api("/api/shop")
  ]);

  state.me = me;
  state.lessons = lessons;
  state.progress = progress;
  state.ranking = ranking;
  state.missions = missions;
  state.shop = shop;

  renderStats();
  renderDashboard();
  renderLearn();
  renderRanking();
  renderMissions();
  renderShop();
}

function renderStats() {
  statsEl.innerHTML = `
    <div class="pill">⭐ ${state.me.totalXp} XP</div>
    <div class="pill">🔥 ${state.me.streakDays}</div>
    <div class="pill">👑 ${state.me.lives}</div>
    <div class="pill">💎 ${state.me.gems}</div>
  `;
}

function renderDashboard() {
  document.getElementById("dashboard").innerHTML = `
    <div class="hero">
      <h2>Bem-vindo, ${state.me.username}</h2>
      <p>Lições concluídas: ${state.me.completedLessons}</p>
    </div>

    <div class="list">
      <div class="item">
        <div>
          <strong>Seu progresso</strong>
          <small>${state.me.completedLessons} lições concluídas</small>
        </div>
        <strong>Nível ${Math.max(1, Math.floor(state.me.totalXp / 40) + 1)}</strong>
      </div>

      <div class="item">
        <div>
          <strong>Ofensiva</strong>
          <small>Continue estudando todos os dias</small>
        </div>
        <strong>${state.me.streakDays} dias</strong>
      </div>
    </div>
  `;
}

function renderLearn() {
  const completed = new Set(state.progress.filter(p => p.completed).map(p => p.lessonId));

  const content = state.activeLesson
    ? renderActiveLesson()
    : `
      <h2>Lições</h2>
      <div class="list">
        ${state.lessons.map(lesson => `
          <div class="item">
            <div>
              <strong>${lesson.title}</strong>
              <small>+${lesson.xpReward} XP ${completed.has(lesson.id) ? "• concluída" : ""}</small>
            </div>
            <button class="action" onclick="openLesson(${lesson.id})">${completed.has(lesson.id) ? "Revisar" : "Começar"}</button>
          </div>
        `).join("")}
      </div>
    `;

  document.getElementById("learn").innerHTML = content;
}

function renderActiveLesson() {
  const lesson = state.activeLesson;
  const feedback = state.feedback
    ? `<div class="feedback ${state.feedback.correct ? "success" : "error"}">${state.feedback.message}</div>`
    : "";

  return `
    <h2>${lesson.title}</h2>
    <p>${lesson.questionText}</p>

    <div class="options">
      ${lesson.options.map(option => `
        <button class="option ${state.selectedOption === option ? "selected" : ""}" onclick="selectOption('${option.replaceAll("'", "\\'")}')">
          ${option}
        </button>
      `).join("")}
    </div>

    ${feedback}

    <div style="display:flex;gap:10px;flex-wrap:wrap;">
      <button class="secondary action" onclick="closeLesson()">Voltar</button>
      <button class="action" onclick="submitAnswer()">Verificar</button>
    </div>
  `;
}

function renderRanking() {
  document.getElementById("ranking").innerHTML = `
    <h2>Ranking</h2>
    <div class="list">
      ${state.ranking.map((user, index) => `
        <div class="item">
          <div>
            <strong>${index + 1}. ${user.username}</strong>
            <small>Posição da tribo</small>
          </div>
          <strong>${user.totalXp} XP</strong>
        </div>
      `).join("")}
    </div>
  `;
}

function renderMissions() {
  document.getElementById("missions").innerHTML = `
    <h2>Missões</h2>
    <div class="list">
      ${state.missions.map(m => `
        <div class="item">
          <div>
            <strong>${m.title}</strong>
            <small>${m.progress}/${m.target} • recompensa ${m.rewardGems}💎</small>
          </div>
          ${
            m.claimed
              ? `<strong>Resgatada</strong>`
              : m.completed
                ? `<button class="action" onclick="claimMission('${m.id}')">Resgatar</button>`
                : `<strong>Em progresso</strong>`
          }
        </div>
      `).join("")}
    </div>
  `;
}

function renderShop() {
  document.getElementById("shop").innerHTML = `
    <h2>Loja</h2>
    <div class="list">
      ${state.shop.map(item => `
        <div class="item">
          <div>
            <strong>${item.title}</strong>
            <small>${item.description}</small>
          </div>
          <button class="action" onclick="buyItem('${item.id}')">${item.price}💎</button>
        </div>
      `).join("")}
    </div>
  `;
}

function openLesson(id) {
  state.activeLesson = state.lessons.find(l => l.id === id);
  state.selectedOption = null;
  state.feedback = null;
  renderLearn();
}

function closeLesson() {
  state.activeLesson = null;
  state.selectedOption = null;
  state.feedback = null;
  renderLearn();
}

function selectOption(option) {
  state.selectedOption = option;
  renderLearn();
}

async function submitAnswer() {
  if (!state.activeLesson || !state.selectedOption) return;

  const res = await api("/api/answer", {
    method: "POST",
    body: JSON.stringify({
      lessonId: state.activeLesson.id,
      selectedOption: state.selectedOption
    })
  });

  state.feedback = {
    correct: res.correct,
    message: res.message
  };

  await loadAll();

  if (res.correct) {
    setTimeout(() => {
      closeLesson();
    }, 800);
  } else {
    renderLearn();
  }
}

async function claimMission(id) {
  await api(`/api/missions/${id}/claim`, { method: "POST" });
  await loadAll();
}

async function buyItem(id) {
  await api(`/api/shop/${id}/buy`, { method: "POST" });
  await loadAll();
}

window.openLesson = openLesson;
window.closeLesson = closeLesson;
window.selectOption = selectOption;
window.submitAnswer = submitAnswer;
window.claimMission = claimMission;
window.buyItem = buyItem;

loadAll();
