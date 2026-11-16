# StockIA Frontend

Interface web para gestão de estoque construída com React, TypeScript, Vite e Tailwind CSS.

## Requisitos

- Node.js 20.19 ou superior
- npm 10 ou superior

## Executar localmente

```bash
npm install
```

Crie o arquivo de variáveis locais:

### Git Bash

```bash
cp .env.example .env
```

### PowerShell ou Prompt de Comando

```powershell
copy .env.example .env
```

Inicie o frontend:

```bash
npm run dev
```

Acesse `http://localhost:5173`.

## Comandos

```bash
npm run dev
npm run typecheck
npm run format:check
npm run build
npm run preview
```

## Backend

A URL pública da API é configurada no arquivo `.env`:

```env
VITE_API_BASE_URL=/api
```

Não coloque senhas, tokens ou chaves privadas em variáveis iniciadas por `VITE_`, pois elas ficam disponíveis no navegador.

O proxy do Vite encaminha `/api` para `http://localhost:8080`, porta padrão do backend atual. Para usar outra porta no PowerShell, execute `$env:API_PROXY_TARGET="http://localhost:8081"` antes de `npm run dev`.

Inicie o backend em `backend/stockia-api` com `mvnw.cmd spring-boot:run`. O PostgreSQL e as variáveis `DB_PASSWORD` e `JWT_SECRET` do `.env` da raiz precisam estar configurados.

O login usa uma conta ativa vinculada a uma empresa ativa. A sessão JWT fica no `sessionStorage` e expira conforme o prazo retornado pela API. O logout encerra a sessão local. Antes de liberar as telas, a aplicação consulta `/api/usuarios/me` para confirmar usuário, empresa e perfil. Menus e rotas administrativos exigem ADMIN.

Login, dashboard, produtos, categorias, fornecedores e movimentações estão integrados. Produtos exigem um fornecedor ativo. O saldo inicial é registrado por entrada de estoque. No ajuste, a quantidade informada representa o saldo final.

O contrato atual de produtos não inclui categoria. Os espaços dos gráficos por categoria e fornecedor sinalizam integração pendente, pois faltam endpoints com essas distribuições. Os seis indicadores, os alertas e as movimentações recentes utilizam os dados reais disponíveis. Auditoria permanece demonstrativa e as áreas administrativas e o assistente continuam pendentes.

Em produção, configure o servidor que publica o frontend para encaminhar `/api` ao backend e retornar `index.html` para as rotas da aplicação.

## Arquivos do Figma Make

O frontend não depende da pasta `.figma`. Se ela estiver presente no arquivo baixado, pode ser removida na cópia local.
