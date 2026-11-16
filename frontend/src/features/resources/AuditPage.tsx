import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { FileClock } from "lucide-react"
import {
  Button,
  Card,
  EmptyState,
  PageHeader,
  SearchInput,
} from "../../components/ui"
import { apiRequest, companyPath } from "../../lib/api-client"
import type { AuditEntry } from "../../lib/contracts"
import { formatDateTime } from "../../lib/formatters"

export function AuditPage() {
  const [search, setSearch] = useState("")
  const path = companyPath("/auditoria")
  const query = useQuery({
    queryKey: [path],
    queryFn: ({ signal }) => apiRequest<AuditEntry[]>(path, { signal }),
    staleTime: 0,
  })
  const rows = (query.data || []).filter((entry) =>
    `${entry.usuarioId ?? ""} ${entry.entidade} ${entry.registroId} ${entry.acao} ${entry.descricao}`
      .toLowerCase()
      .includes(search.toLowerCase()),
  )

  return (
    <div className="page">
      <PageHeader
        title="Histórico e auditoria"
        description="Acompanhe as operações realizadas na empresa."
        action={
          <Button onClick={() => query.refetch()} disabled={query.isFetching}>
            Atualizar
          </Button>
        }
      />
      <Card className="table-card">
        <div className="table-tools">
          <SearchInput
            value={search}
            onChange={setSearch}
            placeholder="Pesquisar no histórico..."
          />
        </div>
        {query.isPending ? (
          <p role="status" className="form-note">
            Carregando histórico...
          </p>
        ) : query.error ? (
          <div role="alert">
            <p className="field-error">{query.error.message}</p>
            <Button onClick={() => query.refetch()}>Tentar novamente</Button>
          </div>
        ) : !rows.length ? (
          <EmptyState
            icon={<FileClock size={30} />}
            title="Nenhum registro encontrado"
            text="As operações da empresa aparecerão aqui."
          />
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  {[
                    "Data e horário",
                    "Usuário",
                    "Ação",
                    "Entidade",
                    "Registro",
                    "Resumo",
                  ].map((header) => (
                    <th key={header}>{header}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {rows.map((entry) => (
                  <tr key={entry.id}>
                    <td>{formatDateTime(entry.createdAt)}</td>
                    <td>
                      {entry.usuarioId == null
                        ? "Sistema"
                        : `ID ${entry.usuarioId}`}
                    </td>
                    <td>{entry.acao.replace(/_/g, " ")}</td>
                    <td>{entry.entidade.replace(/_/g, " ")}</td>
                    <td>{entry.registroId}</td>
                    <td>{entry.descricao}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <div className="pagination">
          <span>{rows.length} registros</span>
        </div>
      </Card>
    </div>
  )
}
