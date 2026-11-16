import { useState, type FormEvent } from "react"
import { useQuery, useQueryClient } from "@tanstack/react-query"
import { X } from "lucide-react"
import { Button, Input } from "../../components/ui"
import { apiRequest, companyPath } from "../../lib/api-client"
import type { Product, Supplier } from "../../lib/contracts"

export function ProductForm({
  close,
  product,
}: {
  close: () => void
  product?: Product
}) {
  const client = useQueryClient()
  const [error, setError] = useState("")
  const [saving, setSaving] = useState(false)
  const suppliers = useQuery({
    queryKey: [companyPath("/fornecedores")],
    queryFn: () => apiRequest<Supplier[]>(companyPath("/fornecedores")),
  })
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    setSaving(true)
    setError("")
    try {
      await apiRequest(
        companyPath("/produtos" + (product ? "/" + product.id : "")),
        {
          method: product ? "PUT" : "POST",
          body: JSON.stringify({
            nome: data.get("nome"),
            sku: data.get("sku"),
            descricao: data.get("descricao"),
            precoCusto: Number(data.get("precoCusto")),
            precoVenda: Number(data.get("precoVenda")),
            estoqueMinimo: Number(data.get("estoqueMinimo")),
            fornecedorId: Number(data.get("fornecedorId")),
          }),
        },
      )
      await client.invalidateQueries()
      close()
    } catch (error) {
      setError((error as Error).message)
    } finally {
      setSaving(false)
    }
  }
  return (
    <div className="modal-backdrop">
      <section
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="product-form-title"
      >
        <header>
          <h2 id="product-form-title">
            {product ? "Editar produto" : "Novo produto"}
          </h2>
          <button
            className="icon-button"
            onClick={close}
            disabled={saving}
            aria-label="Fechar"
          >
            <X size={19} />
          </button>
        </header>
        <form onSubmit={submit}>
          <label>
            Nome do produto
            <Input
              name="nome"
              required
              maxLength={150}
              defaultValue={product?.nome}
            />
          </label>
          <div className="form-row">
            <label>
              SKU
              <Input
                name="sku"
                required
                maxLength={50}
                defaultValue={product?.sku}
              />
            </label>
            <label>
              Fornecedor
              <select
                name="fornecedorId"
                required
                defaultValue={product?.fornecedorId ?? ""}
              >
                <option value="">Selecione</option>
                {suppliers.data
                  ?.filter((s) => s.ativo)
                  .map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.nome}
                    </option>
                  ))}
              </select>
            </label>
          </div>
          {suppliers.isPending && (
            <p role="status">Carregando fornecedores...</p>
          )}
          {suppliers.error && (
            <p className="field-error" role="alert">
              {suppliers.error.message}
            </p>
          )}
          {suppliers.isSuccess && !suppliers.data.some((s) => s.ativo) && (
            <p className="form-note">
              Cadastre um fornecedor antes de cadastrar o produto.
            </p>
          )}
          <label>
            Descrição
            <Input name="descricao" defaultValue={product?.descricao ?? ""} />
          </label>
          <div className="form-row">
            <label>
              Preço de custo
              <Input
                name="precoCusto"
                type="number"
                min="0"
                step="0.01"
                required
                defaultValue={product?.precoCusto}
              />
            </label>
            <label>
              Preço de venda
              <Input
                name="precoVenda"
                type="number"
                min="0"
                step="0.01"
                required
                defaultValue={product?.precoVenda}
              />
            </label>
          </div>
          <label>
            Estoque mínimo
            <Input
              name="estoqueMinimo"
              type="number"
              min="0"
              max="2147483647"
              step="1"
              required
              defaultValue={product?.estoqueMinimo ?? 0}
            />
          </label>
          <div className="form-note">
            Registre o saldo pelo fluxo de movimentações de estoque.
          </div>
          {error && (
            <p className="field-error" role="alert">
              {error}
            </p>
          )}
          <footer>
            <Button
              type="button"
              className="secondary"
              onClick={close}
              disabled={saving}
            >
              Cancelar
            </Button>
            <Button disabled={saving || !suppliers.data?.some((s) => s.ativo)}>
              {saving ? "Salvando..." : "Salvar produto"}
            </Button>
          </footer>
        </form>
      </section>
    </div>
  )
}
