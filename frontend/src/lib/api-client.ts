import { getSession, setSession } from "./session"
const base = (import.meta.env.VITE_API_BASE_URL || "/api").replace(/\/$/, "")
export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
  ) {
    super(message)
  }
}
export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const headers = new Headers(options.headers)
  if (options.body) headers.set("Content-Type", "application/json")
  const session = getSession()
  if (session && path !== "/auth/login")
    headers.set("Authorization", "Bearer " + session.token)
  let response: Response
  try {
    response = await fetch(base + path, { ...options, headers })
  } catch {
    throw new ApiError(
      "Não foi possível conectar à API. Verifique o backend.",
      0,
    )
  }
  const text = await response.text()
  let body
  try {
    body = text ? JSON.parse(text) : undefined
  } catch {
    body = text
  }
  if (!response.ok) {
    if (
      response.status === 401 &&
      path !== "/auth/login" &&
      getSession()?.token === session?.token
    )
      setSession(null)
    const message =
      typeof body === "string" && !body.startsWith("<")
        ? body
        : body?.mensagem ||
          (body?.mensagens
            ? Object.values(body.mensagens).join(" ")
            : undefined)
    throw new ApiError(
      message ||
        (response.status === 403
          ? "Acesso negado."
          : "Não foi possível concluir a solicitação."),
      response.status,
    )
  }
  return body as T
}
export function companyPath(resource: string) {
  const session = getSession()
  if (!session || session.expiresAt <= Date.now()) {
    setSession(null)
    throw new ApiError("Sessão expirada. Entre novamente.", 401)
  }
  return "/empresas/" + session.empresaId + resource
}
