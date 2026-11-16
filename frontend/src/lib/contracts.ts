export interface UserProfile {
  id: number
  empresaId: number
  nome: string
  email: string
  perfil: "ADMIN" | "OPERADOR"
  ativo: boolean
  createdAt: string
}

export interface Product {
  id: number
  empresaId: number
  fornecedorId: number | null
  fornecedorNome: string | null
  nome: string
  sku: string
  descricao: string | null
  precoCusto: number
  precoVenda: number
  quantidadeEstoque: number
  estoqueMinimo: number
  ativo: boolean
}
export interface Category {
  id: number
  nome: string
  descricao: string | null
  ativo: boolean
  createdAt: string
}
export interface Supplier {
  id: number
  nome: string
  documento: string | null
  email: string | null
  telefone: string | null
  contato: string | null
  ativo: boolean
}
export interface Movement {
  id: number
  produtoId: number
  produtoNome: string
  tipo: "ENTRADA" | "SAIDA" | "AJUSTE"
  quantidade: number
  estoqueAnterior: number
  estoquePosterior: number
  observacao: string | null
  createdAt: string
}
export interface Dashboard {
  valorTotalEstoque: number
  produtosCadastrados: number
  unidadesEmEstoque: number
  estoqueBaixo: number
  categoriasAtivas: number
  fornecedoresAtivos: number
}
export function stockStatus(p: Product) {
  return p.quantidadeEstoque === 0
    ? "Sem estoque"
    : p.quantidadeEstoque <= p.estoqueMinimo
      ? "Estoque baixo"
      : "Normal"
}
