export function getDateStringFromInstant(instant: number): string {
  const date = (new Date(instant));
  return `${('0' + date.getHours()).slice(-2)}:${('0' + date.getMinutes()).slice(-2)} del ${('0' + date.getDate()).slice(-2)}/${('0' + (date.getMonth() + 1)).slice(-2)}`;
}
