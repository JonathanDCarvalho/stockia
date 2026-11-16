import { Navigate, createBrowserRouter } from "react-router"
import { AppLayout } from "../components/layout/AppLayout"
import { InventoryPage } from "../features/inventory/InventoryPage"
import { ResourcePage } from "../features/resources/ResourcePage"
import { SettingsPage } from "../features/settings/SettingsPage"
import { RequireAdmin } from "../features/auth/RequireAdmin"

export const router = createBrowserRouter([
  {
    path: "/login",
    lazy: async () => ({
      Component: (await import("../features/auth/LoginPage")).LoginPage,
    }),
  },
  {
    path: "/",
    Component: AppLayout,
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      {
        path: "dashboard",
        lazy: async () => ({
          Component: (await import("../features/dashboard/DashboardPage"))
            .DashboardPage,
        }),
      },
      { path: "produtos", element: <ResourcePage type="products" /> },
      {
        path: "produtos/baixo-estoque",
        element: <ResourcePage type="products" lowStockOnly />,
      },
      { path: "categorias", element: <ResourcePage type="categories" /> },
      { path: "fornecedores", element: <ResourcePage type="suppliers" /> },
      { path: "estoque", Component: InventoryPage },
      {
        Component: RequireAdmin,
        children: [
          { path: "auditoria", element: <ResourcePage type="audit" /> },
          { path: "empresas", element: <ResourcePage type="companies" /> },
          { path: "usuarios", element: <ResourcePage type="users" /> },
        ],
      },
      { path: "configuracoes", Component: SettingsPage },
      { path: "ajuda", element: <ResourcePage type="help" /> },
      { path: "*", element: <Navigate to="/dashboard" replace /> },
    ],
  },
])
