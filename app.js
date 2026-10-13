
const API_BASE = "";

const API = {
  login: "/api/auth/login",
  register: "/api/auth/register",
  logout: "/api/auth/logout",
  me: "/api/auth/me",
  events: "/api/events",
  myBookings: "/api/bookings/my",
  adminBookings: "/api/admin/bookings",
  createEvent: "/api/events",
  seats: id => `/api/events/${encodeURIComponent(id)}/seats`,
  book: "/api/bookings"
};

async function api(path, options = {}) {
  const response = await fetch(API_BASE + path, {
    credentials: "same-origin",
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });

  const text = await response.text();
  let data = null;

  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }

  if (!response.ok) {
    throw new Error(
      data?.message || data?.error || `HTTP ${response.status}`
    );
  }

  return data;
}

function message(el, text, success = false) {
  if (!el) return;
  el.textContent = text;
  el.className = success ? "message success" : "message";
}

function esc(value) {
  return String(value ?? "-").replace(/[&<>"']/g, c => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  }[c]));
}

function arr(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.data)) return data.data;
  if (Array.isArray(data?.events)) return data.events;
  if (Array.isArray(data?.bookings)) return data.bookings;
  return [];
}

function saveSession(user) {
  sessionStorage.setItem("tbUser", JSON.stringify(user));
}

function redirectForRole(role) {
  location.href = String(role || "").toUpperCase().includes("ADMIN")
    ? "admin-dashboard.html"
    : "customer-dashboard.html";
}

async function requireSession(role) {
  try {
    const user = await api(API.me);
    saveSession(user);

    if (
      role &&
      String(user.role).toUpperCase() !== role
    ) {
      redirectForRole(user.role);
      return null;
    }

    return user;
  } catch (error) {
    sessionStorage.removeItem("tbUser");
    location.href = "login.html";
    return null;
  }
}

// LOGIN
const loginForm = document.querySelector("#loginForm");

if (loginForm) {
  loginForm.addEventListener("submit", async event => {
    event.preventDefault();

    const form = new FormData(loginForm);
    const msg = document.querySelector("#message");
    const button = loginForm.querySelector("button");

    button.disabled = true;
    message(msg, "Signing in...", true);

    try {
      const user = await api(API.login, {
        method: "POST",
        body: JSON.stringify({
          username: form.get("username"),
          password: form.get("password")
        })
      });

      saveSession(user);
      redirectForRole(user.role);
    } catch (error) {
      message(msg, error.message);
    } finally {
      button.disabled = false;
    }
  });
}

// REGISTER
const registerForm = document.querySelector("#registerForm");

if (registerForm) {
  registerForm.addEventListener("submit", async event => {
    event.preventDefault();

    const form = new FormData(registerForm);
    const msg = document.querySelector("#message");

    try {
      await api(API.register, {
        method: "POST",
        body: JSON.stringify({
          name: form.get("name"),
          username: form.get("username"),
          password: form.get("password")
        })
      });

      message(msg, "Registration successful. Please login.", true);

      setTimeout(() => {
        location.href = "login.html";
      }, 800);
    } catch (error) {
      message(msg, error.message);
    }
  });
}

// LOGOUT
document.querySelectorAll("[data-logout]").forEach(button => {
  button.addEventListener("click", async () => {
    try {
      await api(API.logout, {
        method: "POST",
        body: "{}"
      });
    } catch (error) {
      // Clear local session even if logout request fails.
    }

    sessionStorage.removeItem("tbUser");
    location.href = "login.html";
  });
});

// REFRESH
document.querySelectorAll("[data-refresh]").forEach(button => {
  button.addEventListener("click", () => location.reload());
});

// FORMAT BOOKING DATE
function formatDate(value) {
  if (!value || value === "-") return "-";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return String(value);
  }

  return date.toLocaleString();
}

// READ FIELD ALIASES
function field(row, ...keys) {
  for (const key of keys) {
    if (row?.[key] !== undefined && row[key] !== null) {
      return row[key];
    }
  }
  return "-";
}

// RENDER BOOKINGS HISTORY
function renderTable(target, rows, admin = false) {
  if (!target) return;

  if (!rows.length) {
    target.innerHTML =
      '<p class="muted">No bookings found yet.</p>';
    return;
  }

  const columns = admin
    ? [
        ["Booking ID", r => field(r, "bookingId", "id")],
        ["Customer", r => field(r, "username", "userName")],
        ["Event", r => field(r, "eventName", "eventTitle", "name", "title")],
        ["Seat", r => field(r, "seatNumber", "seat")],
        ["Booking Date", r => formatDate(field(r, "bookedAt", "bookingTime", "createdAt"))],
        ["Status", r => field(r, "status", "bookingStatus")]
      ]
    : [
        ["Booking ID", r => field(r, "bookingId", "id")],
        ["Event", r => field(r, "eventName", "eventTitle", "name", "title")],
        ["Seat", r => field(r, "seatNumber", "seat")],
        ["Booking Date", r => formatDate(field(r, "bookedAt", "bookingTime", "createdAt"))],
        ["Status", r => field(r, "status", "bookingStatus")]
      ];

  target.innerHTML = `
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            ${columns.map(column =>
              `<th>${esc(column[0])}</th>`
            ).join("")}
          </tr>
        </thead>
        <tbody>
          ${rows.map(row => `
            <tr>
              ${columns.map(column =>
                `<td>${esc(column[1](row))}</td>`
              ).join("")}
            </tr>
          `).join("")}
        </tbody>
      </table>
    </div>
  `;
}

// CUSTOMER DASHBOARD
async function customerDashboard() {
  const user = await requireSession();
  if (!user) return;

  const welcome = document.querySelector("#welcomeName");
  const sideUser = document.querySelector("#sideUser");
  const initial = document.querySelector("#userInitial");

  if (welcome) welcome.textContent = user.name || user.username;
  if (sideUser) sideUser.textContent = user.username;
  if (initial) {
    initial.textContent =
      (user.name || user.username || "C")[0].toUpperCase();
  }

  const eventsEl = document.querySelector("#eventsList");
  const bookingsEl = document.querySelector("#myBookingsList");

  let events = [];
  let bookings = [];

  // LOAD EVENTS
  try {
    events = arr(await api(API.events));
  } catch (error) {
    if (eventsEl) {
      eventsEl.innerHTML =
        `<p class="message">Events API: ${esc(error.message)}</p>`;
    }
  }

  const eventCount = document.querySelector("#eventCount");
  if (eventCount) eventCount.textContent = events.length;

  if (eventsEl && events.length) {
    eventsEl.innerHTML = events.map(event => `
      <article class="event-card">
        <span class="eyebrow">EVENT</span>
        <h3>${esc(field(event, "name", "title"))}</h3>
        <p>${esc(field(event, "eventDate", "eventTime"))}</p>
        <p>${esc(field(event, "venue"))}</p>
        <p>${esc(field(event, "availableSeats"))} seats available</p>
        <div class="price">₹${esc(field(event, "price", "ticketPrice"))}</div>
        <button
          class="btn primary"
          data-event="${esc(event.id)}"
          data-name="${esc(field(event, "name", "title"))}">
          Select seats →
        </button>
      </article>
    `).join("");

    eventsEl.querySelectorAll("[data-event]").forEach(button => {
      button.addEventListener("click", () => {
        location.href =
          `seats.html?eventId=${encodeURIComponent(button.dataset.event)}` +
          `&name=${encodeURIComponent(button.dataset.name)}`;
      });
    });
  } else if (eventsEl && !eventsEl.textContent.includes("API:")) {
    eventsEl.innerHTML =
      '<p class="muted">No events created yet.</p>';
  }

  // LOAD MY BOOKINGS / HISTORY
  if (bookingsEl) {
    bookingsEl.innerHTML =
      '<p class="muted">Loading booking history...</p>';
  }

  try {
    bookings = arr(await api(API.myBookings));

    // Most recent bookings first
    bookings.sort((a, b) => {
      const dateA = new Date(
        field(a, "bookedAt", "bookingTime", "createdAt")
      ).getTime() || 0;

      const dateB = new Date(
        field(b, "bookedAt", "bookingTime", "createdAt")
      ).getTime() || 0;

      return dateB - dateA;
    });

    const bookingCount = document.querySelector("#bookingCount");
    const bookingStatus = document.querySelector("#bookingStatus");

    if (bookingCount) bookingCount.textContent = bookings.length;
    if (bookingStatus) {
      bookingStatus.textContent =
        field(bookings[0], "status", "bookingStatus") || "None";

      if (!bookings.length) bookingStatus.textContent = "None";
    }

    if (bookingsEl) renderTable(bookingsEl, bookings);
  } catch (error) {
    if (bookingsEl) {
      bookingsEl.innerHTML =
        `<p class="message">Bookings API error: ${esc(error.message)}</p>`;
    }
  }
}

// ADMIN DASHBOARD
async function adminDashboard() {
  const user = await requireSession("ADMIN");
  if (!user) return;

  const sideUser = document.querySelector("#sideUser");
  const initial = document.querySelector("#userInitial");

  if (sideUser) sideUser.textContent = user.username;
  if (initial) {
    initial.textContent = (user.name || "A")[0].toUpperCase();
  }

  const target = document.querySelector("#adminBookingsList");

  try {
    const [eventsResponse, bookingsResponse] = await Promise.all([
      api(API.events),
      api(API.adminBookings)
    ]);

    const events = arr(eventsResponse);
    const bookings = arr(bookingsResponse);

    const eventCount = document.querySelector("#eventCount");
    const bookingCount = document.querySelector("#bookingCount");
    const apiStatus = document.querySelector("#apiStatus");

    if (eventCount) eventCount.textContent = events.length;
    if (bookingCount) bookingCount.textContent = bookings.length;
    if (apiStatus) apiStatus.textContent = "Connected";

    renderTable(target, bookings, true);
  } catch (error) {
    const apiStatus = document.querySelector("#apiStatus");
    if (apiStatus) apiStatus.textContent = "Check API";

    if (target) {
      target.innerHTML =
        `<p class="message">${esc(error.message)}</p>`;
    }
  }
}

// CREATE EVENT
const dialog = document.querySelector("#eventDialog");

document.querySelector("#addEventBtn")?.addEventListener("click", () => {
  dialog?.showModal();
});

document.querySelector("#cancelEvent")?.addEventListener("click", () => {
  dialog?.close();
});

document.querySelector("#eventForm")?.addEventListener("submit", async event => {
  event.preventDefault();

  const form = new FormData(event.currentTarget);

  try {
    await api(API.createEvent, {
      method: "POST",
      body: JSON.stringify({
        name: form.get("name"),
        eventDate: form.get("eventDate"),
        venue: form.get("venue"),
        price: Number(form.get("price")),
        seatCount: Number(form.get("seatCount"))
      })
    });

    message(
      document.querySelector("#eventMessage"),
      "Event created successfully.",
      true
    );

    setTimeout(() => location.reload(), 700);
  } catch (error) {
    message(document.querySelector("#eventMessage"), error.message);
  }
});

// SEAT SELECTION AND BOOKING
const seatGrid = document.querySelector("#seatGrid");

if (seatGrid) {
  const query = new URLSearchParams(location.search);
  const eventId = query.get("eventId");
  const selected = new Set();

  const title = document.querySelector("#eventTitle");
  if (title) title.textContent = query.get("name") || "Choose available seats";

  (async () => {
    if (!(await requireSession())) return;

    if (!eventId) {
      seatGrid.innerHTML =
        '<p class="message">Missing event ID.</p>';
      return;
    }

    try {
      const seats = arr(await api(API.seats(eventId)));

      if (!seats.length) {
        seatGrid.innerHTML =
          '<p class="muted">No seats returned for this event.</p>';
        return;
      }

      seatGrid.innerHTML = seats.map(seat => {
        const id = seat.id;
        const booked = seat.status !== "AVAILABLE";

        return `
          <button
            class="seat ${booked ? "booked" : ""}"
            data-id="${esc(id)}"
            ${booked ? "disabled" : ""}>
            ${esc(field(seat, "seatNumber", "number"))}
          </button>
        `;
      }).join("");

      seatGrid.querySelectorAll(".seat:not(.booked)").forEach(button => {
        button.addEventListener("click", () => {
          if (selected.has(button.dataset.id)) {
            selected.delete(button.dataset.id);
            button.classList.remove("selected");
          } else {
            selected.add(button.dataset.id);
            button.classList.add("selected");
          }
        });
      });
    } catch (error) {
      seatGrid.innerHTML =
        `<p class="message">${esc(error.message)}</p>`;
    }
  })();

  document.querySelector("#bookSeatsBtn")?.addEventListener("click", async () => {
    const msg = document.querySelector("#seatMessage");

    if (!selected.size) {
      message(msg, "Please select at least one seat.");
      return;
    }

    const button = document.querySelector("#bookSeatsBtn");
    if (button) button.disabled = true;

    try {
      const result = await api(API.book, {
        method: "POST",
        body: JSON.stringify({
          eventId: Number(eventId),
          seatIds: [...selected].map(Number)
        })
      });

      message(
        msg,
        `Booking confirmed! ${result?.bookingId ? "Booking ID: " + result.bookingId : ""}`,
        true
      );

      setTimeout(() => {
        location.href = "customer-dashboard.html#myBookings";
      }, 1000);
    } catch (error) {
      message(msg, error.message);
      if (button) button.disabled = false;
    }
  });
}

// INITIALIZE PAGE
if (document.body.dataset.page === "customer") {
  customerDashboard();
}

if (document.body.dataset.page === "admin") {
  adminDashboard();
}
