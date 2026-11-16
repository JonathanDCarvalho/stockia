import { CircleHelp } from "lucide-react"
import { Card, PageHeader } from "../../components/ui"
import { LiveResourcePage } from "./LiveResourcePage"
import { AuditPage } from "./AuditPage"
import { CompanyPage } from "./CompanyPage"
import { UsersPage } from "./UsersPage"

type ResourceType = "products" | "categories" | "suppliers" | "audit" | "companies" | "users" | "help"

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
            <p>Conteúdo indisponível no momento.</p>
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
  if (props.type === "audit") return <AuditPage />
  if (props.type === "companies") return <CompanyPage />
  if (props.type === "users") return <UsersPage />
  if (props.type === "help") return <HelpPage />
  return (
    <LiveResourcePage
      key={props.type}
      type={props.type}
      lowStockOnly={props.lowStockOnly}
    />
  )
}
