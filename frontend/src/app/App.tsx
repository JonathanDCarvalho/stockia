import { RouterProvider } from "react-router"
import { QueryClientProvider } from "@tanstack/react-query"
import { queryClient } from "../lib/query-client"
import { router } from "./routes"

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  )
}
