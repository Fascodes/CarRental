/**
 * Wyciąga czytelny komunikat z błędu Axios.
 * Spring's ResponseStatusException.getMessage() zwraca format:
 *   "409 CONFLICT \"reason\""
 * Stripujemy prefiks statusu i otaczające cudzysłowy.
 */
export function parseApiError(err, fallback = 'Wystąpił błąd') {
  const raw =
    err.response?.data?.message ??
    (typeof err.response?.data === 'string' ? err.response.data : null) ??
    fallback

  return (
    String(raw)
      .replace(/^\d{3}\s+\S+\s*/i, '')  // usuwa "409 CONFLICT "
      .replace(/^"|"$/g, '')             // usuwa otaczające cudzysłowy
      .trim() || fallback
  )
}
