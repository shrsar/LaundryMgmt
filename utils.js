const currencyFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 0
});

export function formatMoney(value) {
  return currencyFormatter.format(Number(value ?? 0));
}

export function formatDate(value) {
  if (!value) {
    return "--";
  }
  return new Date(`${value}T00:00:00`).toLocaleDateString("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric"
  });
}

export function formatDateTime(value) {
  if (!value) {
    return "--";
  }
  return new Date(value).toLocaleString("en-IN", {
    dateStyle: "medium",
    timeStyle: "short"
  });
}

export function computeBill(service, quantity, deliveryDate) {
  if (!service || !quantity || !deliveryDate) {
    return 0;
  }

  const qty = Number(quantity) || 0;
  if (qty < 1) {
    return 0;
  }

  const today = new Date();
  const base = Number(service.pricePerItem ?? 0) * qty;
  const delivery = new Date(`${deliveryDate}T00:00:00`);
  const normalizedToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  const differenceInDays = Math.round((delivery - normalizedToday) / (1000 * 60 * 60 * 24));

  if (Number.isNaN(differenceInDays) || differenceInDays < 0) {
    return base;
  }

  return differenceInDays <= 5
    ? base + Number(service.premiumPerDay ?? 0) * (5 - differenceInDays)
    : base;
}

export function statusTone(status) {
  const normalized = (status || "").toUpperCase();
  if (normalized === "ACTIVE" || normalized === "OPEN") {
    return "status status-warm";
  }
  if (normalized === "RECEIVED" || normalized === "RESOLVED") {
    return "status status-cool";
  }
  return "status";
}

export function matchesSearch(targets, query) {
  const normalizedQuery = query.trim().toLowerCase();
  if (!normalizedQuery) {
    return true;
  }
  return targets.some((target) => String(target ?? "").toLowerCase().includes(normalizedQuery));
}
