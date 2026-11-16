import { useState, type FormEvent } from "react"
import { useQueryClient } from "@tanstack/react-query"
import { Button, Input } from "../../components/ui"
import { apiRequest, companyPath } from "../../lib/api-client"
import type { Category, Supplier } from "../../lib/contracts"
export function ResourceForm({
  type,
  item,
  close,
}: {
  type: "categories" | "suppliers"
  item?: Category | Supplier
  close: () => void
}) {
  const [error, setError] = useState("")
  const [saving, setSaving] = useState(false)
  const client = useQueryClient()
  const supplier = type === "suppliers"
  const fields = supplier
    ? ["nome", "documento", "email", "telefone", "contato"]
    : ["nome", "descricao"]
  const labels: Record<string, string> = {
    nome: "Nome",
    documento: "Documento",
    email: "E-mail",
    telefone: "Telefone",
    contato: "Contato",
    descricao: "Descrição",
  }
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    setSaving(true)
    setError("")
    try {
      await apiRequest(
        companyPath(
          (supplier ? "/fornecedores" : "/categorias") +
            (item ? "/" + item.id : ""),
        ),
        {
          method: item ? "PUT" : "POST",
          body: JSON.stringify(
            Object.fromEntries(fields.map((key) => [key, data.get(key)])),
          ),
        },
      )
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
        aria-label={supplier ? "Fornecedor" : "Categoria"}
      >
        <header>
          <h2>
            {item ? "Editar" : "Cadastrar"}{" "}
            {supplier ? "fornecedor" : "categoria"}
          </h2>
        </header>
        <form onSubmit={submit}>
          {fields.map((key) => (
            <label key={key}>
              {labels[key]}
              <Input
                name={key}
                required={key === "nome"}
                type={key === "email" ? "email" : "text"}
                maxLength={
                  key === "nome"
                    ? supplier
                      ? 150
                      : 100
                    : key === "email"
                      ? 150
                      : key === "telefone"
                        ? 20
                        : key === "contato"
                          ? 100
                          : undefined
                }
                defaultValue={
                  item
                    ? String(
                        (item as unknown as Record<string, unknown>)[key] ?? "",
                      )
                    : ""
                }
              />
            </label>
          ))}
          {error && (
            <p role="alert" className="field-error">
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
              {saving ? "Salvando..." : "Salvar"}
            </Button>
          </footer>
        </form>
      </section>
    </div>
  )
}
