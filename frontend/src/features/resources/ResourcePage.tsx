import { LiveResourcePage } from "./LiveResourcePage"
import { useMemo, useState } from "react"
import {
  Building2,
  ChevronLeft,
  ChevronRight,
  CircleHelp,
  Eye,
  FileClock,
  Filter,
  MoreHorizontal,
  Package,
  Plus,
  SearchX,
  Tags,
  Truck,
  UserPlus,
  Users,
} from "lucide-react"
import {
  Badge,
  Button,
  Card,
  EmptyState,
  PageHeader,
  SearchInput,
} from "../../components/ui"
import { auditRows, categories, products, suppliers } from "../../data/demo"
import { formatCurrency } from "../../lib/formatters"
import { ProductForm } from "../products/ProductForm"

type ResourceType = "products" | "categories" | "suppliers" | "audit" | "companies" | "users" | "help"
const config: Record<ResourceType, {
  title: string
  description: string
  action?: string
  icon: typeof Package
}> = {
  products: {
    title: "Produtos",
    description: "Gerencie os produtos e acompanhe seus níveis de estoque.",
    action: "Novo produto",
    icon: Package,
  },
  categories: {
    title: "Categorias",
    description: "Organize os produtos por grupos.",
    action: "Nova categoria",
    icon: Tags,
  },
  suppliers: {
    title: "Fornecedores",
    description: "Gerencie os parceiros que abastecem seu inventário.",
    action: "Novo fornecedor",
    icon: Truck,
  },
  audit: {
    title: "Histórico e auditoria",
    description: "Acompanhe as operações realizadas na empresa.",
    icon: FileClock,
  },
  companies: {
    title: "Empresas",
    description: "Gerencie as empresas às quais você possui acesso.",
    action: "Nova empresa",
    icon: Building2,
  },
  users: {
    title: "Usuários",
    description: "Gerencie acessos e funções da empresa ativa.",
    action: "Convidar usuário",
    icon: Users,
  },
  help: {
    title: "Central de ajuda",
    description: "Encontre orientações para usar o StockIA.",
    icon: CircleHelp,
  },
}

function statusTone(value: string) {
  if (["Normal", "Ativa", "Ativo"].includes(value)) return "success" as const
  if (value === "Estoque baixo") return "warning" as const
  if (["Sem estoque", "Inativo"].includes(value)) return "danger" as const
  return "info" as const
}

function DemoResourcePage({
  type,
  lowStockOnly = false,
}: {
  type: ResourceType
  lowStockOnly?: boolean
}) {
  const [search, setSearch] = useState("")
  const [showForm, setShowForm] = useState(false)
  const page = config[type]
  const productRows = useMemo(
    () =>
      products.filter((item) => {
        const matchesSearch = `${item.name} ${item.sku}`
          .toLowerCase()
          .includes(search.toLowerCase())
        return matchesSearch && (!lowStockOnly || item.stock <= item.min)
      }),
    [search, lowStockOnly],
  )

  const action = page.action ? (
    <Button onClick={() => type === "products" && setShowForm(true)}>
      <Plus size={17} />
      {page.action}
    </Button>
  ) : undefined
  if (type === "help") return <HelpPage />
  if (type === "companies" || type === "users")
    return <PendingPage type={type} />

  return (
    <div className="page">
      <PageHeader
        title={lowStockOnly ? "Produtos com estoque baixo" : page.title}
        description={page.description}
        action={action}
      />
      <div className="demo-banner">
        <span>DEMO</span> Dados ilustrativos — a integração com a API ainda está
        pendente.
      </div>
      <Card className="table-card">
        <div className="table-tools">
          <SearchInput
            value={search}
            onChange={setSearch}
            placeholder={
              type === "audit"
                ? "Pesquisar no histórico..."
                : `Buscar em ${page.title.toLowerCase()}...`
            }
          />
          <div>
            <button className="filter-button">
              <Filter size={16} />
              Filtros
            </button>
            <button className="filter-button">
              Mais recentes <ChevronRight size={15} />
            </button>
          </div>
        </div>
        {type === "products" && <ProductsTable rows={productRows} />}
        {type === "categories" && (
          <GenericTable
            headers={[
              "Nome",
              "Descrição",
              "Produtos",
              "Status",
              "Criada em",
              "",
            ]}
            rows={categories}
          />
        )}
        {type === "suppliers" && (
          <GenericTable
            headers={[
              "Fornecedor",
              "CNPJ",
              "E-mail",
              "Telefone",
              "Produtos",
              "Status",
              "",
            ]}
            rows={suppliers}
          />
        )}
        {type === "audit" && (
          <GenericTable
            headers={[
              "Data e horário",
              "Usuário",
              "Ação",
              "Entidade",
              "Registro",
              "Resumo",
              "",
            ]}
            rows={auditRows}
          />
        )}
        <div className="pagination">
          <span>
            Mostrando 1–{type === "products" ? productRows.length : 4} de{" "}
            {type === "products" ? productRows.length : 4}
          </span>
          <div>
            <button disabled>
              <ChevronLeft size={16} />
            </button>
            <button className="selected">1</button>
            <button disabled>
              <ChevronRight size={16} />
            </button>
          </div>
        </div>
      </Card>
      {showForm && <ProductForm close={() => setShowForm(false)} />}
    </div>
  )
}

function ProductsTable({ rows }: { rows: typeof products }) {
  if (!rows.length)
    return (
      <EmptyState
        icon={<SearchX size={30} />}
        title="Nenhum produto encontrado"
        text="Revise os filtros ou tente um termo diferente."
      />
    )
  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            {[
              "Produto",
              "Categoria",
              "Fornecedor",
              "Custo unitário",
              "Quantidade",
              "Mínimo",
              "Status",
              "",
            ].map((item) => (
              <th key={item}>{item}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((item) => (
            <tr key={item.sku}>
              <td>
                <div className="product-cell">
                  <div>
                    <Package size={18} />
                  </div>
                  <span>
                    <strong>{item.name}</strong>
                    <small>{item.sku}</small>
                  </span>
                </div>
              </td>
              <td>{item.category}</td>
              <td>{item.supplier}</td>
              <td>{formatCurrency(item.cost)}</td>
              <td>
                <strong>
                  {item.stock} {item.unit}
                </strong>
              </td>
              <td>
                {item.min} {item.unit}
              </td>
              <td>
                <Badge tone={statusTone(item.status)}>{item.status}</Badge>
              </td>
              <td>
                <button className="icon-button">
                  <MoreHorizontal size={18} />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function GenericTable({
  headers,
  rows,
}: {
  headers: string[]
  rows: string[][]
}) {
  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            {headers.map((item, index) => (
              <th key={`${item}-${index}`}>{item}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => (
            <tr key={rowIndex}>
              {row.map((cell, index) => (
                <td key={`${cell}-${index}`}>
                  {index === row.length - 1 &&
                  ["Ativa", "Ativo"].includes(cell) ? (
                    <Badge tone="success">{cell}</Badge>
                  ) : index === 0 ? (
                    <strong>{cell}</strong>
                  ) : (
                    cell
                  )}
                </td>
              ))}
              <td>
                <button className="icon-button">
                  <Eye size={17} />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function PendingPage({ type }: { type: "companies" | "users" }) {
  const page = config[type]
  return (
    <div className="page">
      <PageHeader title={page.title} description={page.description} />
      <Card>
        <EmptyState
          icon={
            type === "users" ? <UserPlus size={32} /> : <Building2 size={32} />
          }
          title="Integração administrativa pendente"
          text="Esta área será habilitada quando os endpoints de autorização e associações estiverem disponíveis."
        />
      </Card>
    </div>
  )
}

function HelpPage() {
  return (
    <div className="page">
      <PageHeader
        title="Central de ajuda"
        description="Encontre orientações para usar o StockIA."
      />
      <div className="help-grid">
        {[
          "Primeiros passos",
          "Produtos e categorias",
          "Controle de estoque",
          "Empresas e acessos",
        ].map((item) => (
          <Card key={item}>
            <CircleHelp size={22} />
            <h3>{item}</h3>
            <p>Guias e documentação estarão disponíveis em breve.</p>
            <button>
              Saiba mais <ChevronRight size={15} />
            </button>
          </Card>
        ))}
      </div>
    </div>
  )
}

export function ResourcePage(props: {
  type: ResourceType
  lowStockOnly?: boolean
}) {
  if (
    props.type === "products" ||
    props.type === "categories" ||
    props.type === "suppliers"
  )
    return (
      <LiveResourcePage
        key={props.type}
        type={props.type}
        lowStockOnly={props.lowStockOnly}
      />
    )
  return <DemoResourcePage {...props} />
}
