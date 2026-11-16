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

Login, dashboard, produtos, categorias, fornecedores, movimentações, auditoria, usuários e dados da empresa estão integrados. Produtos exigem um fornecedor ativo. O cadastro permite informar uma quantidade inicial, registrada como entrada de estoque na mesma transação do produto. O campo opcional `quantidadeInicial` foi adicionado ao contrato de cadastro e assume zero quando omitido. No ajuste, a quantidade informada representa o saldo final.

O gráfico de pizza mostra produtos com estoque adequado e baixo a partir dos indicadores reais do dashboard. Produtos podem ser associados a uma categoria no cadastro ou na edição. O campo opcional `categoriaId` aceita uma categoria ativa da mesma empresa ou `null` para remover a associação. Uma categoria desativada já associada pode ser mantida ao editar o produto. A categoria aparece na lista de produtos.

Administradores podem consultar a auditoria, cadastrar e desativar usuários e consultar, renomear ou desativar a empresa da conta. A desativação da empresa encerra a sessão e impede o acesso dos usuários vinculados. O CNPJ é somente leitura, conforme o contrato atual de atualização. A listagem global e o cadastro de empresas estão bloqueados pela segurança do backend. O assistente de IA continua indisponível.

Em produção, configure o servidor que publica o frontend para encaminhar `/api` ao backend e retornar `index.html` para as rotas da aplicação.

## Arquivos do Figma Make

O frontend não depende da pasta `.figma`. Se ela estiver presente no arquivo baixado, pode ser removida na cópia local.
