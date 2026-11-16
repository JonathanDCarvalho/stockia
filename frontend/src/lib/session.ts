import { useSyncExternalStore } from "react"
export interface Session {
  token: string
  tipo: string
  expiresIn: number
  usuarioId: number
  empresaId: number
  nome: string
  perfil: string
  expiresAt: number
}
const key = "stockia:session"
let session: Session | null = null
try {
  const saved = JSON.parse(sessionStorage.getItem(key) || "null")
  if (saved?.token && saved?.empresaId && saved.expiresAt > Date.now())
    session = saved
} catch {
  sessionStorage.removeItem(key)
}
const listeners = new Set<() => void>()
export function getSession() {
  return session
}
export function setSession(value: Session | null) {
  session = value
  if (value) sessionStorage.setItem(key, JSON.stringify(value))
  else sessionStorage.removeItem(key)
  listeners.forEach((listener) => listener())
}
export function useSession() {
  return useSyncExternalStore((listener) => {
    listeners.add(listener)
    return () => {
      listeners.delete(listener)
    }
  }, getSession)
}
