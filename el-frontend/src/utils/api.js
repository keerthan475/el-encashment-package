export async function getApiErrorMessage(response, fallbackMessage) {
  let body = null;

  try {
    body = await response.json();
  } catch {
    try {
      const text = await response.text();
      if (text?.trim()) return text;
    } catch {
      return fallbackMessage;
    }
    return fallbackMessage;
  }

  if (typeof body === "string" && body.trim()) return body;
  if (body?.message) return body.message;
  if (body?.error && typeof body.error === "string") return body.error;
  return fallbackMessage;
}
