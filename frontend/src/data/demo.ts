export const products = [
  {
    name: "Notebook Pro 14",
    sku: "NB-PRO-014",
    category: "Eletrônicos",
    supplier: "Tech Supply",
    cost: 4899.9,
    stock: 18,
    min: 8,
    unit: "un.",
    status: "Normal",
  },
  {
    name: 'Monitor UltraWide 29"',
    sku: "MON-UW-029",
    category: "Eletrônicos",
    supplier: "Vision Tech",
    cost: 1699.9,
    stock: 5,
    min: 10,
    unit: "un.",
    status: "Estoque baixo",
  },
  {
    name: "Cadeira Ergonômica Air",
    sku: "CAD-AIR-002",
    category: "Mobiliário",
    supplier: "Office Prime",
    cost: 1249.5,
    stock: 0,
    min: 4,
    unit: "un.",
    status: "Sem estoque",
  },
  {
    name: "Teclado Mecânico TKL",
    sku: "TEC-TKL-004",
    category: "Periféricos",
    supplier: "Tech Supply",
    cost: 389.9,
    stock: 32,
    min: 12,
    unit: "un.",
    status: "Normal",
  },
  {
    name: "Headset Focus USB",
    sku: "HDS-FCS-009",
    category: "Periféricos",
    supplier: "Sound Corp",
    cost: 279.9,
    stock: 7,
    min: 10,
    unit: "un.",
    status: "Estoque baixo",
  },
  {
    name: "Hub USB-C 8 em 1",
    sku: "HUB-USC-008",
    category: "Acessórios",
    supplier: "Tech Supply",
    cost: 219.5,
    stock: 24,
    min: 8,
    unit: "un.",
    status: "Normal",
  },
]

export const categoryChart = [
  { name: "Eletrônicos", value: 128400 },
  { name: "Mobiliário", value: 87600 },
  { name: "Periféricos", value: 64900 },
  { name: "Acessórios", value: 38200 },
  { name: "Insumos", value: 18900 },
]

export const supplierChart = [
  { name: "Tech Supply", value: 112500 },
  { name: "Office Prime", value: 78900 },
  { name: "Vision Tech", value: 62500 },
  { name: "Sound Corp", value: 41300 },
]

export const movements = [
  {
    product: "Notebook Pro 14",
    type: "Entrada",
    qty: 12,
    user: "Marina Costa",
    date: "2025-05-28T14:32:00",
  },
  {
    product: "Headset Focus USB",
    type: "Saída",
    qty: 3,
    user: "Lucas Almeida",
    date: "2025-05-28T11:18:00",
  },
  {
    product: 'Monitor UltraWide 29"',
    type: "Ajuste",
    qty: 2,
    user: "Marina Costa",
    date: "2025-05-27T16:45:00",
  },
  {
    product: "Teclado Mecânico TKL",
    type: "Entrada",
    qty: 20,
    user: "Rafael Lima",
    date: "2025-05-27T09:06:00",
  },
]

export const categories = [
  [
    "Eletrônicos",
    "Equipamentos e dispositivos eletrônicos",
    "42",
    "Ativa",
    "14/01/2025",
  ],
  [
    "Periféricos",
    "Acessórios para estações de trabalho",
    "31",
    "Ativa",
    "18/01/2025",
  ],
  ["Mobiliário", "Mesas, cadeiras e organização", "18", "Ativa", "02/02/2025"],
  [
    "Acessórios",
    "Adaptadores, cabos e utilidades",
    "27",
    "Ativa",
    "10/02/2025",
  ],
]

export const suppliers = [
  [
    "Tech Supply",
    "—",
    "contato@techsupply.com",
    "(11) 4000-1200",
    "38",
    "Ativo",
  ],
  [
    "Office Prime",
    "—",
    "comercial@officeprime.com",
    "(11) 3500-8080",
    "21",
    "Ativo",
  ],
  [
    "Vision Tech",
    "—",
    "vendas@visiontech.com",
    "(21) 3200-4100",
    "15",
    "Ativo",
  ],
  [
    "Sound Corp",
    "—",
    "atendimento@soundcorp.com",
    "(31) 3010-2200",
    "9",
    "Ativo",
  ],
]

export const auditRows = [
  [
    "28/05/2025 14:32",
    "Marina Costa",
    "Entrada registrada",
    "Movimentação",
    "MOV-1284",
    "+12 un. em Notebook Pro 14",
  ],
  [
    "28/05/2025 11:18",
    "Lucas Almeida",
    "Saída registrada",
    "Movimentação",
    "MOV-1283",
    "-3 un. em Headset Focus USB",
  ],
  [
    "27/05/2025 16:45",
    "Marina Costa",
    "Produto atualizado",
    "Produto",
    "PRD-0291",
    "Estoque mínimo alterado",
  ],
  [
    "27/05/2025 09:06",
    "Rafael Lima",
    "Fornecedor atualizado",
    "Fornecedor",
    "FOR-0012",
    "Dados de contato atualizados",
  ],
]
