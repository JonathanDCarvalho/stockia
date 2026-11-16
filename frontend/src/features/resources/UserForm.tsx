import { useState, type FormEvent } from "react"
import { useQueryClient } from "@tanstack/react-query"
import { Button, Input } from "../../components/ui"
import { apiRequest, companyPath } from "../../lib/api-client"

export function UserForm({ close }: { close: () => void }) {
  const [error, setError] = useState("")
  const [saving, setSaving] = useState(false)
  const client = useQueryClient()

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    const senha = String(data.get("senha"))
    setError("")
    if (new TextEncoder().encode(senha).length > 72) {
      setError("A senha é muito longa. Use uma senha menor.")
      return
    }
    setSaving(true)
    try {
      await apiRequest(companyPath("/usuarios"), {
        method: "POST",
        body: JSON.stringify({
          nome: String(data.get("nome")).trim(),
          email: String(data.get("email")).trim(),
          senha,
          perfil: data.get("perfil"),
        }),
      })
      await client.invalidateQueries()
      close()
    } catch (error) {
      setError((error as Error).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="modal-backdrop">
      <section
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="user-form-title"
      >
        <header>
          <h2 id="user-form-title">Novo usuário</h2>
        </header>
        <form onSubmit={submit}>
          <label>
            Nome
            <Input name="nome" required maxLength={150} autoComplete="name" />
          </label>
          <label>
            E-mail
            <Input
              name="email"
              type="email"
              required
              maxLength={255}
              autoComplete="off"
            />
          </label>
          <label>
            Senha
            <Input
              name="senha"
              type="password"
              required
              minLength={12}
              maxLength={72}
              autoComplete="new-password"
            />
          </label>
          <label>
            Perfil
            <select name="perfil" defaultValue="OPERADOR">
              <option value="OPERADOR">Operador</option>
              <option value="ADMIN">Administrador</option>
            </select>
          </label>
          {error && (
            <p className="field-error" role="alert">
              {error}
            </p>
          )}
          <footer>
            <Button
              type="button"
              className="secondary"
              disabled={saving}
              onClick={close}
            >
              Cancelar
            </Button>
            <Button disabled={saving}>
              {saving ? "Salvando..." : "Salvar usuário"}
            </Button>
          </footer>
        </form>
      </section>
    </div>
  )
}
