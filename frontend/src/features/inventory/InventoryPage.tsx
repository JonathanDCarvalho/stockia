import { useState, type FormEvent } from "react"
import { useQuery, useQueryClient } from "@tanstack/react-query"
import { Plus } from "lucide-react"
import { Badge, Button, Card, Input, PageHeader } from "../../components/ui"
import { apiRequest, companyPath } from "../../lib/api-client"
import { stockStatus, type Product, type Movement } from "../../lib/contracts"
import { formatDateTime } from "../../lib/formatters"
export function InventoryPage() {
  const [tab, setTab] = useState<"stock" | "history">("stock")
  const [showMovement, setShowMovement] = useState(false)
  const [type, setType] = useState<Movement["tipo"]>("ENTRADA")
  const [error, setError] = useState("")
  const [saving, setSaving] = useState(false)
  const client = useQueryClient()
  const products = useQuery({
    queryKey: [companyPath("/produtos")],
    queryFn: () => apiRequest<Product[]>(companyPath("/produtos")),
  })
  const movements = useQuery({
    queryKey: [companyPath("/movimentacoes")],
    queryFn: () => apiRequest<Movement[]>(companyPath("/movimentacoes")),
  })
  const rows = products.data?.filter((p) => p.ativo) || []
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    setSaving(true)
    setError("")
    try {
      await apiRequest(
        companyPath("/produtos/" + data.get("produtoId") + "/movimentacoes"),
        {
          method: "POST",
          body: JSON.stringify({
            tipo: type,
            quantidade: Number(data.get("quantidade")),
            observacao: data.get("observacao"),
          }),
        },
      )
      await client.invalidateQueries()
      setShowMovement(false)
      setTab("history")
    } catch (error) {
      setError((error as Error).message)
    } finally {
      setSaving(false)
    }
  }
  const queryError = products.error || movements.error
  return (
    <div className="page">
      <PageHeader
        title="Estoque e movimentações"
        description="Acompanhe entradas, saídas e ajustes de inventário."
        action={
          <Button
            disabled={!rows.length}
            onClick={() => {
              setError("")
              setShowMovement(true)
            }}
          >
            <Plus size={17} />
            Registrar movimentação
          </Button>
        }
      />
      <div className="mini-stats">
        {[
          [
            "Saldo positivo",
            rows.filter((p) => p.quantidadeEstoque > 0).length,
          ],
          ["Sem estoque", rows.filter((p) => p.quantidadeEstoque === 0).length],
          [
            "Abaixo do mínimo",
            rows.filter((p) => p.quantidadeEstoque <= p.estoqueMinimo).length,
          ],
          [
            "Total de unidades",
            rows.reduce((sum, p) => sum + p.quantidadeEstoque, 0),
          ],
        ].map(([label, value]) => (
          <Card key={label}>
            <span>{label}</span>
            <strong>{value}</strong>
          </Card>
        ))}
      </div>
      <div className="tabs">
        <button
          className={tab === "stock" ? "active" : ""}
          onClick={() => setTab("stock")}
        >
          Posição de estoque
        </button>
        <button
          className={tab === "history" ? "active" : ""}
          onClick={() => setTab("history")}
        >
          Histórico de movimentações
        </button>
      </div>
      <Card className="table-card">
        {products.isPending || movements.isPending ? (
          <p role="status">Carregando...</p>
        ) : queryError ? (
          <div role="alert">
            <p className="field-error">{queryError.message}</p>
            <Button
              onClick={() => {
                products.refetch()
                movements.refetch()
              }}
            >
              Tentar novamente
            </Button>
          </div>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  {(tab === "stock"
                    ? [
                        "Produto",
                        "SKU",
                        "Quantidade atual",
                        "Mínimo",
                        "Situação",
                      ]
                    : [
                        "Data e horário",
                        "Produto",
                        "Tipo",
                        "Quantidade",
                        "Saldo anterior",
                        "Saldo posterior",
                        "Observação",
                      ]
                  ).map((h) => (
                    <th key={h}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {tab === "stock"
                  ? rows.map((p) => (
                      <tr key={p.id}>
                        <td>
                          <strong>{p.nome}</strong>
                        </td>
                        <td>{p.sku}</td>
                        <td>{p.quantidadeEstoque}</td>
                        <td>{p.estoqueMinimo}</td>
                        <td>
                          <Badge
                            tone={
                              stockStatus(p) === "Normal"
                                ? "success"
                                : "warning"
                            }
                          >
                            {stockStatus(p)}
                          </Badge>
                        </td>
                      </tr>
                    ))
                  : movements.data?.map((m) => (
                      <tr key={m.id}>
                        <td>{formatDateTime(m.createdAt)}</td>
                        <td>
                          <strong>{m.produtoNome}</strong>
                        </td>
                        <td>
                          <Badge
                            tone={
                              m.tipo === "ENTRADA"
                                ? "success"
                                : m.tipo === "SAIDA"
                                  ? "danger"
                                  : "info"
                            }
                          >
                            {m.tipo === "ENTRADA"
                              ? "Entrada"
                              : m.tipo === "SAIDA"
                                ? "Saída"
                                : "Ajuste"}
                          </Badge>
                        </td>
                        <td>{m.quantidade}</td>
                        <td>{m.estoqueAnterior}</td>
                        <td>{m.estoquePosterior}</td>
                        <td>{m.observacao || "—"}</td>
                      </tr>
                    ))}
              </tbody>
            </table>
            {!(tab === "stock" ? rows.length : movements.data?.length) && (
              <p className="form-note">Nenhum registro encontrado.</p>
            )}
          </div>
        )}
      </Card>
      {showMovement && (
        <div className="modal-backdrop">
          <section
            className="modal small"
            role="dialog"
            aria-modal="true"
            aria-labelledby="movement-title"
          >
            <header>
              <h2 id="movement-title">Registrar movimentação</h2>
            </header>
            <form onSubmit={submit}>
              <label>
                Produto
                <select name="produtoId" required>
                  <option value="">Selecione</option>
                  {rows.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.nome} — saldo {p.quantidadeEstoque}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Tipo
                <select
                  value={type}
                  onChange={(e) => setType(e.target.value as Movement["tipo"])}
                >
                  <option value="ENTRADA">Entrada</option>
                  <option value="SAIDA">Saída</option>
                  <option value="AJUSTE">Ajuste</option>
                </select>
              </label>
              <label>
                {type === "AJUSTE" ? "Saldo final do estoque" : "Quantidade"}
                <Input
                  name="quantidade"
                  type="number"
                  min={type === "AJUSTE" ? 0 : 1}
                  max="2147483647"
                  step="1"
                  required
                />
              </label>
              <label>
                Observação
                <Input name="observacao" maxLength={500} />
              </label>
              {type === "AJUSTE" && (
                <p className="form-note">
                  O ajuste define a quantidade final do estoque.
                </p>
              )}
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
                  onClick={() => setShowMovement(false)}
                >
                  Cancelar
                </Button>
                <Button disabled={saving}>
                  {saving ? "Registrando..." : "Confirmar movimentação"}
                </Button>
              </footer>
            </form>
          </section>
        </div>
      )}
    </div>
  )
}
