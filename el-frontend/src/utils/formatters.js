export const formatDate = (value) => {
  if (!value) {
    return "-";
  }

  const [year, month, day] = String(value).split("-");
  if (!year || !month || !day) {
    return value;
  }

  return `${day}/${month}/${year}`;
};

export const toDateInputValue = (value) => {
  if (!value) {
    return "";
  }

  if (String(value).includes("/")) {
    return value;
  }

  return formatDate(value);
};

export const parseDateInput = (value) => {
  if (!value) {
    return "";
  }

  const trimmed = String(value).trim();

  if (trimmed.includes("-")) {
    const [year, month, day] = trimmed.split("-");
    if (!year || !month || !day) {
      return "";
    }
    return `${year}-${month.padStart(2, "0")}-${day.padStart(2, "0")}`;
  }

  const [day, month, year] = trimmed.split("/");
  if (!day || !month || !year) {
    return "";
  }

  if (year.length !== 4) {
    return "";
  }

  const iso = `${year}-${month.padStart(2, "0")}-${day.padStart(2, "0")}`;
  const date = new Date(`${iso}T00:00:00`);
  if (Number.isNaN(date.getTime())) {
    return "";
  }

  if (date.toISOString().slice(0, 10) !== iso) {
    return "";
  }

  return iso;
};

export const formatAmount = (value) => {
  if (value === null || value === undefined || value === "") {
    return "-";
  }

  const amount = Number(value);
  return Number.isFinite(amount) ? amount.toFixed(2) : value;
};

export const formatDisgType = (value) => {
  if (value === 1) {
    return "Officer";
  }

  if (value === 2 || value === 3) {
    return "Staff";
  }

  return "-";
};
