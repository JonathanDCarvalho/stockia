import { Outlet } from "react-router"
import { ShieldCheck } from "lucide-react"
import { Card, EmptyState } from "../../components/ui"
import { useSession } from "../../lib/session"

export function RequireAdmin() {
  const session = useSession()
  if (session?.perfil === "ADMIN") return <Outlet />

  return (
    <div className="page">
      <Card>
        <EmptyState
          icon={<ShieldCheck size={30} />}
          title="Acesso restrito"
          text="Esta área exige um perfil de administrador."
        />
      </Card>
    </div>
  )
}
