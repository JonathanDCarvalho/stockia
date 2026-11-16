import { useState } from "react"
import { useQuery, useQueryClient } from "@tanstack/react-query"
import { Package, Plus, SearchX } from "lucide-react"
import {
  Badge,
  Button,
  Card,
  EmptyState,
  PageHeader,
  SearchInput,
} from "../../components/ui"
import { apiRequest, companyPath } from "../../lib/api-client"
import {
  stockStatus,
  type Product,
  type Category,
  type Supplier,
} from "../../lib/contracts"
import { formatCurrency } from "../../lib/formatters"
import { ProductForm } from "../products/ProductForm"
import { ResourceForm } from "./ResourceForm"
type Row = Product | Category | Supplier
export function LiveResourcePage({
  type,
  lowStockOnly,
}: {
  type: "products" | "categories" | "suppliers"
  lowStockOnly?: boolean
}) {
  const [search, setSearch] = useState("")
  const [editing, setEditing] = useState<Row | null | undefined>()
  const [error, setError] = useState("")
  const [busy, setBusy] = useState(false)
  const client = useQueryClient()
  const resource = {
    products: "produtos",
    categories: "categorias",
    suppliers: "fornecedores",
  }[type]
  const title = {
    products: "Produtos",
    categories: "Categorias",
    suppliers: "Fornecedores",
  }[type]
  const path = companyPath("/" + resource)
  const query = useQuery({
    queryKey: [path],
    queryFn: () => apiRequest<Row[]>(path),
  })
  const rows =
    query.data?.filter(
      (row) =>
        (
          row.nome +
          ("sku" in row ? ` ${row.sku} ${row.categoriaNome ?? ""}` : "")
        )
          .toLowerCase()
          .includes(search.toLowerCase()) &&
        (!lowStockOnly ||
          ("quantidadeEstoque" in row &&
            row.quantidadeEstoque <= row.estoqueMinimo)),
    ) ?? []
  async function deactivate(row: Row) {
    if (!window.confirm("Desativar " + row.nome + "?")) return
    setBusy(true)
    setError("")
    try {
      await apiRequest(
        path + "/" + row.id + (type === "products" ? "" : "/desativar"),
        { method: type === "products" ? "DELETE" : "PATCH" },
      )
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
        title={lowStockOnly ? "Produtos com estoque baixo" : title}
        description="Gerencie os registros da empresa ativa."
        action={
          <Button onClick={() => setEditing(null)}>
            <Plus size={17} />{" "}
            {type === "products"
              ? "Novo produto"
              : type === "categories"
                ? "Nova categoria"
                : "Novo fornecedor"}
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
            placeholder={"Buscar em " + title.toLowerCase() + "..."}
          />
        </div>
        {query.isPending ? (
          <p role="status">Carregando...</p>
        ) : query.error ? (
          <div role="alert">
            <p className="field-error">{query.error.message}</p>
            <Button onClick={() => query.refetch()}>Tentar novamente</Button>
          </div>
        ) : !rows.length ? (
          <EmptyState
            icon={<SearchX size={30} />}
            title="Nenhum registro encontrado"
            text="Cadastre um registro ou revise a busca."
          />
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  {(type === "products"
                    ? [
                        "Produto",
                        "Categoria",
                        "Fornecedor",
                        "Custo unitário",
                        "Preço de venda",
                        "Quantidade",
                        "Mínimo",
                        "Status",
                        "Ações",
                      ]
                    : type === "categories"
                      ? ["Nome", "Descrição", "Status", "Ações"]
                      : [
                          "Fornecedor",
                          "Documento",
                          "E-mail",
                          "Telefone",
                          "Contato",
                          "Status",
                          "Ações",
                        ]
                  ).map((h) => (
                    <th key={h}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.id}>
                    <td>
                      <div className="product-cell">
                        <div>
                          <Package size={18} />
                        </div>
                        <span>
                          <strong>{row.nome}</strong>
                          {"sku" in row && <small>{row.sku}</small>}
                        </span>
                      </div>
                    </td>
                    {"quantidadeEstoque" in row ? (
                      <>
                        <td>{row.categoriaNome || "Sem categoria"}</td>
                        <td>{row.fornecedorNome || "—"}</td>
                        <td>{formatCurrency(row.precoCusto)}</td>
                        <td>{formatCurrency(row.precoVenda)}</td>
                        <td>{row.quantidadeEstoque}</td>
                        <td>{row.estoqueMinimo}</td>
                      </>
                    ) : "documento" in row ? (
                      <>
                        <td>{row.documento || "—"}</td>
                        <td>{row.email || "—"}</td>
                        <td>{row.telefone || "—"}</td>
                        <td>{row.contato || "—"}</td>
                      </>
                    ) : (
                      <td>{row.descricao || "—"}</td>
                    )}
                    <td>
                      <Badge
                        tone={
                          !row.ativo
                            ? "danger"
                            : "quantidadeEstoque" in row &&
                                stockStatus(row) !== "Normal"
                              ? "warning"
                              : "success"
                        }
                      >
                        {!row.ativo
                          ? "Inativo"
                          : "quantidadeEstoque" in row
                            ? stockStatus(row)
                            : "Ativo"}
                      </Badge>
                    </td>
                    <td>
                      <button
                        className="filter-button"
                        disabled={busy || !row.ativo}
                        onClick={() => setEditing(row)}
                      >
                        Editar
                      </button>
                      <button
                        className="filter-button"
                        disabled={busy || !row.ativo}
                        onClick={() => deactivate(row)}
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
          <span>{rows.length} registros</span>
        </div>
      </Card>
      {editing !== undefined &&
        (type === "products" ? (
          <ProductForm
            product={editing as Product | null ?? undefined}
            close={() => setEditing(undefined)}
          />
        ) : (
          <ResourceForm
            type={type}
            item={editing as Category | Supplier | null ?? undefined}
            close={() => setEditing(undefined)}
          />
        ))}
    </div>
  )
}
