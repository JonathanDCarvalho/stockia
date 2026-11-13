ALTER TABLE fornecedores
DROP CONSTRAINT ck_fornecedores_documento;

ALTER TABLE fornecedores
ADD CONSTRAINT ck_fornecedores_documento
CHECK (
    documento IS NULL
    OR documento ~ '^([0-9]{11}|[A-Z0-9]{12}[0-9]{2})$'
);