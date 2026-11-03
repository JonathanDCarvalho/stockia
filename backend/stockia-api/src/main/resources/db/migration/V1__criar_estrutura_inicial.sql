CREATE TABLE empresas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cnpj VARCHAR(14) UNIQUE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE produtos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    descricao TEXT,
    preco_custo NUMERIC(12, 2) NOT NULL,
    preco_venda NUMERIC(12, 2) NOT NULL,
    quantidade_estoque INTEGER NOT NULL DEFAULT 0,
    estoque_minimo INTEGER NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_produtos_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id),

    CONSTRAINT uk_produtos_empresa_sku
        UNIQUE (empresa_id, sku),

    CONSTRAINT chk_produtos_preco_custo
        CHECK (preco_custo >= 0),

    CONSTRAINT chk_produtos_preco_venda
        CHECK (preco_venda >= 0),

    CONSTRAINT chk_produtos_quantidade
        CHECK (quantidade_estoque >= 0),

    CONSTRAINT chk_produtos_estoque_minimo
        CHECK (estoque_minimo >= 0)
);