import { useState, type FormEvent } from "react"
import { useQuery, useQueryClient } from "@tanstack/react-query"
import { X } from "lucide-react"
import { Button, Input } from "../../components/ui"
import { apiRequest, companyPath } from "../../lib/api-client"
import type { Category, Product, Supplier } from "../../lib/contracts"

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
  const [categoriaId, setCategoriaId] = useState(
    product?.categoriaId?.toString() ?? "",
  )
  const suppliers = useQuery({
    queryKey: [companyPath("/fornecedores")],
    queryFn: () => apiRequest<Supplier[]>(companyPath("/fornecedores")),
  })
  const categories = useQuery({
    queryKey: [companyPath("/categorias")],
    queryFn: () => apiRequest<Category[]>(companyPath("/categorias")),
    staleTime: 0,
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
            categoriaId: data.get("categoriaId")
              ? Number(data.get("categoriaId"))
              : null,
            ...(!product
              ? { quantidadeInicial: Number(data.get("quantidadeInicial")) }
              : {}),
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
          <div className="form-row">
            <label>
              Descrição
              <Input name="descricao" defaultValue={product?.descricao ?? ""} />
            </label>
            <label>
              Categoria
              <select
                name="categoriaId"
                value={categoriaId}
                onChange={(event) => setCategoriaId(event.target.value)}
                disabled={categories.isPending || categories.isError}
              >
                <option value="">Sem categoria</option>
                {product?.categoriaId &&
                  !categories.data?.some(
                    (category) => category.id === product.categoriaId,
                  ) && (
                    <option value={product.categoriaId}>
                      {product.categoriaNome}
                      {categories.isSuccess ? " (inativa)" : ""}
                    </option>
                  )}
                {categories.data
                  ?.filter((category) => category.ativo)
                  .map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.nome}
                    </option>
                  ))}
              </select>
            </label>
          </div>
          {categories.isPending && (
            <p role="status">Carregando categorias...</p>
          )}
          {categories.error && (
            <div role="alert">
              <p className="field-error">{categories.error.message}</p>
              <Button
                type="button"
                className="secondary"
                onClick={() => categories.refetch()}
              >
                Tentar novamente
              </Button>
            </div>
          )}
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
          <div className={!product ? "form-row" : undefined}>
            {!product && (
              <label>
                Quantidade inicial
                <Input
                  name="quantidadeInicial"
                  type="number"
                  min="0"
                  max="2147483647"
                  step="1"
                  required
                  defaultValue={0}
                />
              </label>
            )}
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
            <Button
              disabled={
                saving ||
                categories.isPending ||
                categories.isError ||
                !suppliers.data?.some((s) => s.ativo)
              }
            >
              {saving ? "Salvando..." : "Salvar produto"}
            </Button>
          </footer>
        </form>
      </section>
    </div>
  )
}
