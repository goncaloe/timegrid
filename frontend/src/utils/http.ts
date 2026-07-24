// o backend devolve os erros como application/problem+json; aceitamos
// qualquer content-type que termine em json (application/json, +json, ...)
export function isJsonContentType(contentType: string | null | undefined): boolean {
  return contentType !== null && contentType !== undefined && contentType.includes('json');
}