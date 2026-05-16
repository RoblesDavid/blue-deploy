package com.roblesdavid.blue_deploy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Spring Boot Blue Deploy.
 */
@SpringBootApplication
public class BlueDeployApplication {

	/**
	 * Arranca el contexto de Spring y expone los endpoints de la API.
	 *
	 * @param args argumentos de ejecución del proceso Java
	 */
	public static void main(String[] args) {
		SpringApplication.run(BlueDeployApplication.class, args);
	}

}
