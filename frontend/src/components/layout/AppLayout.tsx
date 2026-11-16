import { useSession, setSession, getSession } from "../../lib/session"
import { queryClient } from "../../lib/query-client"
import { useQuery } from "@tanstack/react-query"
import { apiRequest } from "../../lib/api-client"
import type { UserProfile } from "../../lib/contracts"
import { useEffect, useState } from "react"
import {
  Navigate,
  NavLink,
  Outlet,
  useLocation,
  useNavigate,
} from "react-router"
import {
  Bell,
  Bot,
  Boxes,
  Building2,
  ChartNoAxesCombined,
  ChevronDown,
  CircleHelp,
  ClipboardList,
  LayoutDashboard,
  LogOut,
  Menu,
  Package,
  Settings,
  Tags,
  Truck,
  Users,
  X,
  Send,
} from "lucide-react"
import { Button } from "../ui"

type NavigationGroup = {
  label: string
  items: {
    to: string
    label: string
    icon: typeof Package
    adminOnly?: boolean
  }[]
}

const groups: NavigationGroup[] = [
  {
    label: "Visão geral",
    items: [{ to: "/dashboard", label: "Dashboard", icon: LayoutDashboard }],
  },
  {
    label: "Gestão",
    items: [
      { to: "/produtos", label: "Produtos", icon: Package },
      { to: "/categorias", label: "Categorias", icon: Tags },
      { to: "/fornecedores", label: "Fornecedores", icon: Truck },
      { to: "/estoque", label: "Estoque", icon: Boxes },
    ],
  },
  {
    label: "Controle",
    items: [
      {
        to: "/auditoria",
        label: "Histórico e auditoria",
        icon: ClipboardList,
        adminOnly: true,
      },
    ],
  },
  {
    label: "Sistema",
    items: [
      { to: "/empresas", label: "Empresas", icon: Building2, adminOnly: true },
      { to: "/usuarios", label: "Usuários", icon: Users, adminOnly: true },
      { to: "/configuracoes", label: "Configurações", icon: Settings },
      { to: "/ajuda", label: "Ajuda", icon: CircleHelp },
    ],
  },
]

type SidebarProps = {
  open: boolean
  close: () => void
}

function Sidebar({ open, close }: SidebarProps) {
  const session = useSession()
  const visibleGroups = groups
    .map((group) => ({
      ...group,
      items: group.items.filter(
        (item) => !item.adminOnly || session?.perfil === "ADMIN",
      ),
    }))
    .filter((group) => group.items.length > 0)
  return (
    <>
      {open && (
        <button
          className="sidebar-overlay"
          aria-label="Fechar menu"
          onClick={close}
        />
      )}
      <aside className={`sidebar ${open ? "sidebar-open" : ""}`}>
        <div className="brand">
          <div className="brand-mark">
            <Boxes size={22} />
          </div>
          <div>
            <strong>
              Stock<span>IA</span>
            </strong>
            <small>Intelligent Inventory</small>
          </div>
          <button
            className="icon-button mobile-close"
            onClick={close}
            aria-label="Fechar menu"
          >
            <X size={20} />
          </button>
        </div>
        <nav>
          {visibleGroups.map((group) => (
            <div className="nav-group" key={group.label}>
              <span>{group.label}</span>
              {group.items.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  onClick={close}
                  className={({ isActive }) => (isActive ? "active" : "")}
                >
                  <item.icon size={18} strokeWidth={1.8} />
                  <span>{item.label}</span>
                </NavLink>
              ))}
            </div>
          ))}
        </nav>
        <div className="sidebar-foot">
          <ChartNoAxesCombined size={17} />
          <div>
            <strong>Sessão autenticada</strong>
            <span>StockIA API</span>
          </div>
        </div>
      </aside>
    </>
  )
}

function Header({ openMenu }: { openMenu: () => void }) {
  const [profileOpen, setProfileOpen] = useState(false)
  const location = useLocation()
  const navigate = useNavigate()
  const current = groups
    .flatMap((group) => group.items)
    .find((item) => location.pathname.startsWith(item.to))
  return (
    <header className="topbar">
      <div className="topbar-title">
        <button
          className="icon-button menu-button"
          onClick={openMenu}
          aria-label="Abrir menu"
        >
          <Menu size={21} />
        </button>
        <div>
          <span>StockIA /</span>
          <strong>{current?.label ?? "Dashboard"}</strong>
        </div>
      </div>
      <div className="topbar-actions">
        <div className="company-select" aria-label="Empresa vinculada à conta">
          <Building2 size={16} />
          <span>Empresa {getSession()?.empresaId}</span>
        </div>
        <button className="icon-button" aria-label="Notificações">
          <Bell size={19} />
        </button>
        <div className="user-menu-wrap">
          <button
            className="user-menu"
            onClick={() => setProfileOpen(!profileOpen)}
            aria-expanded={profileOpen}
            aria-haspopup="menu"
          >
            <div className="avatar">
              {getSession()?.nome.slice(0, 2).toUpperCase()}
            </div>
            <div>
              <strong>{getSession()?.nome}</strong>
              <span>{getSession()?.perfil}</span>
            </div>
            <ChevronDown
              className={profileOpen ? "chevron-open" : ""}
              size={15}
            />
          </button>
          {profileOpen && (
            <div className="profile-dropdown" role="menu">
              <div>
                <strong>{getSession()?.nome}</strong>
                <span>Sessão autenticada</span>
              </div>
              <button
                role="menuitem"
                onClick={() => {
                  setProfileOpen(false)
                  setSession(null)
                  queryClient.clear()
                  navigate("/login")
                }}
              >
                <LogOut size={16} />
                Sair da aplicação
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}

function Assistant() {
  const [open, setOpen] = useState(false)
  const [message, setMessage] = useState("")
  const suggestions = ["Produtos abaixo do mínimo", "Últimas movimentações"]
  return (
    <>
      {open && (
        <section className="assistant-panel" aria-label="Assistente StockIA">
          <header>
            <div className="assistant-icon">
              <Bot size={20} />
            </div>
            <div>
              <strong>Assistente StockIA</strong>
              <span>Integração pendente</span>
            </div>
            <button className="icon-button" onClick={() => setOpen(false)}>
              <X size={18} />
            </button>
          </header>
          <div className="assistant-body">
            <div className="assistant-message">
              <strong>Olá, sou o assistente StockIA.</strong>
              <p>
                O serviço de IA ainda não está conectado. Quando disponível,
                poderei consultar dados autorizados da empresa ativa.
              </p>
            </div>
            <div className="suggestions">
              {suggestions.map((item) => (
                <button key={item} onClick={() => setMessage(item)}>
                  {item}
                </button>
              ))}
            </div>
          </div>
          <form onSubmit={(event) => event.preventDefault()}>
            <input
              value={message}
              onChange={(event) => setMessage(event.target.value)}
              placeholder="Pergunte sobre seu estoque..."
            />
            <Button type="submit" aria-label="Enviar" disabled>
              <Send size={17} />
            </Button>
          </form>
        </section>
      )}
      <button
        className="assistant-button"
        onClick={() => setOpen(!open)}
        aria-label="Abrir assistente"
      >
        <Bot size={23} />
      </button>
    </>
  )
}

export function AppLayout() {
  const [menuOpen, setMenuOpen] = useState(false)
  const session = useSession()
  const profile = useQuery({
    queryKey: ["/usuarios/me", session?.usuarioId],
    queryFn: ({ signal }) =>
      apiRequest<UserProfile>("/usuarios/me", { signal }),
    enabled: !!session,
    retry: false,
    refetchOnMount: "always",
    refetchOnWindowFocus: true,
  })
  useEffect(() => {
    if (!session || !profile.data) return
    const user = profile.data
    if (!user.ativo) {
      setSession(null)
      queryClient.clear()
      return
    }
    if (
      session.usuarioId !== user.id ||
      session.empresaId !== user.empresaId ||
      session.nome !== user.nome ||
      session.perfil !== user.perfil
    ) {
      setSession({
        ...session,
        usuarioId: user.id,
        empresaId: user.empresaId,
        nome: user.nome,
        perfil: user.perfil,
      })
    }
  }, [session, profile.data])
  useEffect(() => {
    if (!session) return
    const timer = window.setTimeout(
      () => {
        setSession(null)
        queryClient.clear()
      },
      Math.max(0, session.expiresAt - Date.now()),
    )
    return () => window.clearTimeout(timer)
  }, [session])
  if (!session || session.expiresAt <= Date.now())
    return <Navigate to="/login" replace />
  if (profile.error) {
    return (
      <div className="page" role="alert">
        <p className="field-error">{profile.error.message}</p>
        <Button onClick={() => profile.refetch()}>Tentar novamente</Button>
        <Button
          className="secondary"
          onClick={() => {
            setSession(null)
            queryClient.clear()
          }}
        >
          Voltar ao login
        </Button>
      </div>
    )
  }
  if (
    !profile.data ||
    !profile.data.ativo ||
    profile.data.id !== session.usuarioId ||
    profile.data.empresaId !== session.empresaId ||
    profile.data.perfil !== session.perfil
  ) {
    return (
      <div className="page" role="status">
        Verificando sua conta...
      </div>
    )
  }
  return (
    <div className="app-shell">
      <Sidebar open={menuOpen} close={() => setMenuOpen(false)} />
      <div className="app-content">
        <Header openMenu={() => setMenuOpen(true)} />
        <main>
          <Outlet />
        </main>
      </div>
      <Assistant />
    </div>
  )
}
