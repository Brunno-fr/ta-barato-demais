package com.tabaratodemais.user;

import com.tabaratodemais.TabaratoDemaisApplication;
import java.io.Console;
import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

public final class AdminSetup {
    private AdminSetup() {}

    public static void run() {
        Console console = System.console();
        if (console == null)
            throw new IllegalStateException("Abra criar-admin.ps1 em um terminal interativo. Não redirecione a entrada.");
        var app = new SpringApplication(TabaratoDemaisApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setAdditionalProfiles("bootstrap-admin");
        try (var context = app.run()) {
            if (context.getBean(AdminUserRepository.class).count() != 0)
                throw new IllegalStateException("Já existe uma conta; nenhuma alteração foi realizada.");
            String email = console.readLine("E-mail do administrador: ");
            char[] password = console.readPassword("Senha (mínimo de 12 caracteres; digitação oculta): ");
            char[] confirmation = null;
            try {
                confirmation = console.readPassword("Confirme a senha: ");
                if (password == null || confirmation == null || !Arrays.equals(password, confirmation))
                    throw new IllegalArgumentException("As senhas não coincidem ou a entrada foi cancelada.");
                context.getBean(FirstAdminService.class).create(email, password);
                console.printf("Administrador criado. Acesse http://localhost:8080/admin/login%n");
            } finally {
                if (password != null) Arrays.fill(password, '\0');
                if (confirmation != null) Arrays.fill(confirmation, '\0');
            }
        }
    }
}
