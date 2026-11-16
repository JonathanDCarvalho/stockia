import { useState, type FormEvent } from "react"
import { useQuery, useQueryClient } from "@tanstack/react-query"
import { Badge, Button, Card, Input, PageHeader } from "../../components/ui"
import { apiRequest, companyPath } from "../../lib/api-client"
import type { Company } from "../../lib/contracts"
import { setSession } from "../../lib/session"
import { formatDateTime } from "../../lib/formatters"

export function CompanyPage() {
  const [editing, setEditing] = useState(false)
  const [error, setError] = useState("")
  const [busy, setBusy] = useState(false)
  const client = useQueryClient()
  const path = companyPath("")
  const query = useQuery({
    queryKey: [path],
    queryFn: ({ signal }) => apiRequest<Company>(path, { signal }),
  })

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    setBusy(true)
    setError("")
    try {
      await apiRequest(path, {
        method: "PUT",
        body: JSON.stringify({
          nome: String(data.get("nome")).trim(),
          cnpj: query.data?.cnpj,
        }),
      })
      await client.invalidateQueries()
      setEditing(false)
    } catch (error) {
      setError((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function deactivate() {
    if (
      !window.confirm(
        "Desativar sua empresa? Todos os usuários perderão o acesso ao sistema.",
      )
    )
      return
    setBusy(true)
    setError("")
    try {
      await apiRequest(path, { method: "DELETE" })
      setSession(null)
      client.clear()
    } catch (error) {
      setError((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page">
      <PageHeader
        title="Empresas"
        description="Dados da empresa vinculada à sua conta."
      />
      {error && (
        <p className="field-error" role="alert">
          {error}
        </p>
      )}
      <Card className="table-card">
        {query.isPending ? (
          <p className="form-note" role="status">
            Carregando empresa...
          </p>
        ) : query.error ? (
          <div role="alert">
            <p className="field-error">{query.error.message}</p>
            <Button onClick={() => query.refetch()}>Tentar novamente</Button>
          </div>
        ) : (
          query.data && (
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    {["Empresa", "CNPJ", "Status", "Criada em", "Ações"].map(
                      (header) => (
                        <th key={header}>{header}</th>
                      ),
                    )}
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>
                      <strong>{query.data.nome}</strong>
                    </td>
                    <td>{query.data.cnpj || "—"}</td>
                    <td>
                      <Badge tone={query.data.ativo ? "success" : "danger"}>
                        {query.data.ativo ? "Ativa" : "Inativa"}
                      </Badge>
                    </td>
                    <td>{formatDateTime(query.data.createdAt)}</td>
                    <td>
                      <button
                        className="filter-button"
                        disabled={busy}
                        onClick={() => {
                          setError("")
                          setEditing(true)
                        }}
                      >
                        Editar
                      </button>
                      <button
                        className="filter-button"
                        disabled={busy}
                        onClick={deactivate}
                      >
                        Desativar empresa
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          )
        )}
      </Card>
      {editing && query.data && (
        <div className="modal-backdrop">
          <section
            className="modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="company-form-title"
          >
            <header>
              <h2 id="company-form-title">Editar empresa</h2>
            </header>
            <form onSubmit={save}>
              <label>
                Nome
                <Input
                  name="nome"
                  required
                  maxLength={150}
                  defaultValue={query.data.nome}
                />
              </label>
              <label>
                CNPJ
                <Input value={query.data.cnpj || ""} readOnly />
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
                  disabled={busy}
                  onClick={() => setEditing(false)}
                >
                  Cancelar
                </Button>
                <Button disabled={busy}>
                  {busy ? "Salvando..." : "Salvar empresa"}
                </Button>
              </footer>
            </form>
          </section>
        </div>
      )}
    </div>
  )
}
