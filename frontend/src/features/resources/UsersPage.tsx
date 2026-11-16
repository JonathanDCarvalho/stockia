import { useState } from "react"
import { useQuery, useQueryClient } from "@tanstack/react-query"
import { Plus, Users } from "lucide-react"
import {
  Badge,
  Button,
  Card,
  EmptyState,
  PageHeader,
  SearchInput,
} from "../../components/ui"
import { apiRequest, companyPath } from "../../lib/api-client"
import type { UserProfile } from "../../lib/contracts"
import { useSession } from "../../lib/session"
import { UserForm } from "./UserForm"

export function UsersPage() {
  const [search, setSearch] = useState("")
  const [showForm, setShowForm] = useState(false)
  const [error, setError] = useState("")
  const [busy, setBusy] = useState(false)
  const session = useSession()
  const client = useQueryClient()
  const path = companyPath("/usuarios")
  const query = useQuery({
    queryKey: [path],
    queryFn: ({ signal }) => apiRequest<UserProfile[]>(path, { signal }),
  })
  const rows = (query.data || []).filter((user) =>
    `${user.nome} ${user.email} ${user.perfil}`
      .toLowerCase()
      .includes(search.toLowerCase()),
  )

  async function deactivate(user: UserProfile) {
    if (!window.confirm(`Desativar o usuário ${user.nome}?`)) return
    setBusy(true)
    setError("")
    try {
      await apiRequest(`${path}/${user.id}/desativar`, { method: "PATCH" })
      await client.invalidateQueries()
    } catch (error) {
      setError((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page">
      <PageHeader
        title="Usuários"
        description="Gerencie os acessos da sua empresa."
        action={
          <Button onClick={() => setShowForm(true)}>
            <Plus size={17} />
            Novo usuário
          </Button>
        }
      />
      {error && (
        <p className="field-error" role="alert">
          {error}
        </p>
      )}
      <Card className="table-card">
        <div className="table-tools">
          <SearchInput
            value={search}
            onChange={setSearch}
            placeholder="Buscar usuários..."
          />
        </div>
        {query.isPending ? (
          <p className="form-note" role="status">
            Carregando usuários...
          </p>
        ) : query.error ? (
          <div role="alert">
            <p className="field-error">{query.error.message}</p>
            <Button onClick={() => query.refetch()}>Tentar novamente</Button>
          </div>
        ) : !rows.length ? (
          <EmptyState
            icon={<Users size={30} />}
            title="Nenhum usuário encontrado"
            text="Revise a busca ou cadastre um usuário."
          />
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  {["Nome", "E-mail", "Perfil", "Status", "Ações"].map(
                    (header) => (
                      <th key={header}>{header}</th>
                    ),
                  )}
                </tr>
              </thead>
              <tbody>
                {rows.map((user) => (
                  <tr key={user.id}>
                    <td>
                      <strong>{user.nome}</strong>
                      {user.id === session?.usuarioId && (
                        <small>Sua conta</small>
                      )}
                    </td>
                    <td>{user.email}</td>
                    <td>
                      {user.perfil === "ADMIN" ? "Administrador" : "Operador"}
                    </td>
                    <td>
                      <Badge tone={user.ativo ? "success" : "danger"}>
                        {user.ativo ? "Ativo" : "Inativo"}
                      </Badge>
                    </td>
                    <td>
                      <button
                        className="filter-button"
                        disabled={
                          busy || !user.ativo || user.id === session?.usuarioId
                        }
                        onClick={() => deactivate(user)}
                      >
                        Desativar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <div className="pagination">
          <span>{rows.length} usuários</span>
        </div>
      </Card>
      {showForm && <UserForm close={() => setShowForm(false)} />}
    </div>
  )
}
