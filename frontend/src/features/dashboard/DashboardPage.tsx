import { useNavigate } from "react-router"
import {
  AlertTriangle,
  Boxes,
  ChevronRight,
  CircleDollarSign,
  Package,
  Tags,
  Truck,
  TrendingDown,
  ArrowDownToLine,
  ArrowUpFromLine,
  SlidersHorizontal,
} from "lucide-react"
import { Card, EmptyState, PageHeader } from "../../components/ui"
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts"
import { useQuery } from "@tanstack/react-query"
import { apiRequest, companyPath } from "../../lib/api-client"
import type { Dashboard, Product, Movement } from "../../lib/contracts"
import {
  formatCurrency,
  formatDateTime,
  formatNumber,
} from "../../lib/formatters"

export function DashboardPage() {
  const navigate = useNavigate()
  const summary = useQuery({
    queryKey: [companyPath("/dashboard")],
    queryFn: () => apiRequest<Dashboard>(companyPath("/dashboard")),
  })
  const productQuery = useQuery({
    queryKey: [companyPath("/produtos")],
    queryFn: () => apiRequest<Product[]>(companyPath("/produtos")),
  })
  const movementQuery = useQuery({
    queryKey: [companyPath("/movimentacoes")],
    queryFn: () => apiRequest<Movement[]>(companyPath("/movimentacoes")),
  })
  if (summary.isPending)
    return (
      <div className="page" role="status">
        Carregando dashboard...
      </div>
    )
  const error = summary.error
  if (error)
    return (
      <div className="page" role="alert">
        <p className="field-error">{error.message}</p>
        <button
          className="button"
          onClick={() => {
            summary.refetch()
            productQuery.refetch()
            movementQuery.refetch()
          }}
        >
          Tentar novamente
        </button>
      </div>
    )
  const data = summary.data!
  const stockChart = [
    {
      name: "Estoque adequado",
      value: Math.max(0, data.produtosCadastrados - data.estoqueBaixo),
      color: "#8b5cf6",
    },
    { name: "Estoque baixo", value: data.estoqueBaixo, color: "#f59e0b" },
  ]
  const stats = [
    {
      label: "Valor total do estoque",
      value: formatCurrency(data.valorTotalEstoque),
      icon: CircleDollarSign,
      tone: "purple",
    },
    {
      label: "Produtos cadastrados",
      value: formatNumber(data.produtosCadastrados),
      icon: Package,
      tone: "blue",
    },
    {
      label: "Unidades em estoque",
      value: formatNumber(data.unidadesEmEstoque),
      icon: Boxes,
      tone: "green",
    },
    {
      label: "Estoque baixo",
      value: formatNumber(data.estoqueBaixo),
      icon: TrendingDown,
      tone: "rose",
      action: true,
    },
    {
      label: "Categorias ativas",
      value: formatNumber(data.categoriasAtivas),
      icon: Tags,
      tone: "amber",
    },
    {
      label: "Fornecedores ativos",
      value: formatNumber(data.fornecedoresAtivos),
      icon: Truck,
      tone: "cyan",
    },
  ]
  const products = (productQuery.data || [])
    .filter((p) => p.ativo)
    .map((p) => ({
      name: p.nome,
      sku: p.sku,
      stock: p.quantidadeEstoque,
      min: p.estoqueMinimo,
      supplier: p.fornecedorNome || "—",
    }))
  const movements = (movementQuery.data || []).map((m) => ({
    product: m.produtoNome,
    type:
      m.tipo === "ENTRADA"
        ? "Entrada"
        : m.tipo === "SAIDA"
          ? "Saída"
          : "Ajuste",
    date: m.createdAt,
    observation: m.observacao,
    qty: m.estoquePosterior - m.estoqueAnterior,
  }))
  return (
    <div className="page">
      <PageHeader
        title="Dashboard"
        description="Visão geral da operação e do inventário."
      />
      <div className="stats-grid">
        {stats.map((stat) => (
          <Card
            key={stat.label}
            className={`stat-card ${stat.action ? "clickable" : ""}`}
          >
            <div className={`stat-icon ${stat.tone}`}>
              <stat.icon size={20} />
            </div>
            <div>
              <span>{stat.label}</span>
              <strong>{stat.value}</strong>
            </div>
            {stat.action && (
              <button
                aria-label="Ver estoque baixo"
                onClick={() => navigate("/produtos/baixo-estoque")}
              >
                <ChevronRight size={18} />
              </button>
            )}
          </Card>
        ))}
      </div>
      <Card className="chart-card mt-4">
        <div className="card-heading">
          <div>
            <h2>Situação dos produtos</h2>
            <p>Produtos ativos por nível de estoque</p>
          </div>
          <button onClick={() => navigate("/produtos")}>
            Ver produtos <ChevronRight size={15} />
          </button>
        </div>
        {data.produtosCadastrados === 0 ? (
          <EmptyState
            icon={<Package size={30} />}
            title="Nenhum produto cadastrado"
            text="O gráfico aparecerá quando houver produtos cadastrados."
          />
        ) : (
          <div className="donut-wrap">
            <ResponsiveContainer width="55%" height={260}>
              <PieChart accessibilityLayer>
                <Pie
                  data={stockChart.filter((item) => item.value > 0)}
                  dataKey="value"
                  nameKey="name"
                  innerRadius={62}
                  outerRadius={94}
                  paddingAngle={3}
                  stroke="none"
                >
                  {stockChart
                    .filter((item) => item.value > 0)
                    .map((item) => (
                      <Cell key={item.name} fill={item.color} />
                    ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    background: "#10152a",
                    border: "1px solid #2b3354",
                    borderRadius: 8,
                    color: "#f1f5f9",
                  }}
                  itemStyle={{ color: "#f1f5f9" }}
                  formatter={(value) =>
                    `${formatNumber(Number(value))} produtos`
                  }
                />
              </PieChart>
            </ResponsiveContainer>
            <div className="chart-legend">
              {stockChart.map((item) => (
                <div key={item.name}>
                  <i style={{ background: item.color }} />
                  <span>{item.name}</span>
                  <strong>{formatNumber(item.value)}</strong>
                </div>
              ))}
            </div>
          </div>
        )}
      </Card>
      <div className="bottom-grid">
        <Card className="list-card">
          <div className="card-heading">
            <div>
              <h2>Alertas de estoque</h2>
              <p>Produtos que precisam de atenção</p>
            </div>
            <button onClick={() => navigate("/produtos/baixo-estoque")}>
              Ver todos <ChevronRight size={15} />
            </button>
          </div>
          <div className="alert-list">
            {productQuery.isPending && (
              <p role="status">Carregando alertas...</p>
            )}
            {productQuery.error && (
              <div role="alert">
                <p className="field-error">{productQuery.error.message}</p>
                <button onClick={() => productQuery.refetch()}>
                  Tentar novamente
                </button>
              </div>
            )}
            {productQuery.isSuccess &&
              !products.some((p) => p.stock <= p.min) && (
                <p>Nenhum alerta de estoque.</p>
              )}
            {products
              .filter((product) => product.stock <= product.min)
              .slice(0, 3)
              .map((product) => (
                <div key={product.sku}>
                  <div
                    className={`alert-icon ${
                      product.stock === 0 ? "critical" : ""
                    }`}
                  >
                    <AlertTriangle size={17} />
                  </div>
                  <div>
                    <strong>{product.name}</strong>
                    <span>
                      {product.sku} · {product.supplier}
                    </span>
                  </div>
                  <div className="stock-count">
                    <strong>{product.stock}</strong>
                    <span>mín. {product.min}</span>
                  </div>
                </div>
              ))}
          </div>
        </Card>
        <Card className="list-card">
          <div className="card-heading">
            <div>
              <h2>Movimentações recentes</h2>
              <p>Últimas operações confirmadas</p>
            </div>
            <button onClick={() => navigate("/estoque")}>
              Ver histórico <ChevronRight size={15} />
            </button>
          </div>
          <div className="movement-list">
            {movementQuery.isPending && (
              <p role="status">Carregando movimentações...</p>
            )}
            {movementQuery.error && (
              <div role="alert">
                <p className="field-error">{movementQuery.error.message}</p>
                <button onClick={() => movementQuery.refetch()}>
                  Tentar novamente
                </button>
              </div>
            )}
            {movementQuery.isSuccess && !movements.length && (
              <p>Nenhuma movimentação registrada.</p>
            )}
            {movements.slice(0, 3).map((item, index) => (
              <div key={`${item.product}-${index}`}>
                <div className={`movement-icon ${item.type.toLowerCase()}`}>
                  {item.type === "Entrada" ? (
                    <ArrowDownToLine size={16} />
                  ) : item.type === "Saída" ? (
                    <ArrowUpFromLine size={16} />
                  ) : (
                    <SlidersHorizontal size={16} />
                  )}
                </div>
                <div>
                  <strong>{item.product}</strong>
                  <span>
                    {formatDateTime(item.date)}
                    {item.observation && ` · ${item.observation}`}
                  </span>
                </div>
                <b>
                  {item.qty > 0 ? "+" : ""}
                  {item.qty}
                </b>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  )
}
