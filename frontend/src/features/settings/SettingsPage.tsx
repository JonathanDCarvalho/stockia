import { useState } from "react"
import { Bell, Building2, Moon, UserRound } from "lucide-react"
import { Card, PageHeader } from "../../components/ui"

export function SettingsPage() {
  const [compact, setCompact] = useState(false)
  return (
    <div className="page">
      <PageHeader
        title="Configurações"
        description="Gerencie suas preferências do StockIA."
      />
      <div className="settings-layout">
        <nav>
          {[
            ["Aparência", Moon],
            ["Perfil", UserRound],
            ["Empresa", Building2],
            ["Notificações", Bell],
          ].map(([label, Icon], index) => (
            <button
              className={index === 0 ? "active" : ""}
              key={label as string}
            >
              {<Icon size={17} />}
              {label as string}
            </button>
          ))}
        </nav>
        <Card>
          <h2>Preferências de interface</h2>
          <p>Personalize a experiência neste dispositivo.</p>
          <div className="setting-row">
            <div>
              <strong>Tema escuro</strong>
              <span>O tema padrão do StockIA.</span>
            </div>
            <button className="switch on" aria-label="Tema escuro ativado">
              <i />
            </button>
          </div>
          <div className="setting-row">
            <div>
              <strong>Visualização compacta</strong>
              <span>Reduza o espaçamento das tabelas.</span>
            </div>
            <button
              className={`switch ${compact ? "on" : ""}`}
              onClick={() => setCompact(!compact)}
              aria-pressed={compact}
            >
              <i />
            </button>
          </div>
        </Card>
      </div>
    </div>
  )
}
