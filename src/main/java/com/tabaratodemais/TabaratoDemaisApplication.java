package com.tabaratodemais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TabaratoDemaisApplication {

	public static void main(String[] args) {
		if (args.length == 1 && "--setup-admin".equals(args[0])) {
			com.tabaratodemais.user.AdminSetup.run();
			return;
		}
		SpringApplication.run(TabaratoDemaisApplication.class, args);
	}

}
