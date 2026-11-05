package com.stockia.api.validation;

import java.util.Locale;

public final class CnpjValidator {

    private CnpjValidator() {
    }

    public static String normalizar(String cnpj) {
        if (cnpj == null) {
            return null;
        }

        return cnpj
                .replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase(Locale.ROOT);
    }

    public static boolean isValid(String cnpj) {

        String cnpjNormalizado = normalizar(cnpj);

        if (cnpjNormalizado == null ||
                !cnpjNormalizado.matches("[A-Z0-9]{12}[0-9]{2}")) {
            return false;
        }

        if (cnpjNormalizado.chars().distinct().count() == 1) {
            return false;
        }

        String base = cnpjNormalizado.substring(0, 12);

        int primeiroDigito = calcularDigito(base);

        String baseComPrimeiroDigito = base + primeiroDigito;

        int segundoDigito = calcularDigito(baseComPrimeiroDigito);

        String digitosCalculados = "" + primeiroDigito + segundoDigito;

        return digitosCalculados.equals(cnpjNormalizado.substring(12));
    }

    private static int calcularDigito(String base) {

        int soma = 0;

        for (int i = 0; i < base.length(); i++) {

            char caractere = base.charAt(i);

            int valor = caractere - 48;

            int peso = ((base.length() - 1 - i) % 8) + 2;

            soma += valor * peso;
        }

        int resto = soma % 11;

        return resto < 2 ? 0 : 11 - resto;
    }
}