package vn.kltn;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KltnApplication {

	public static void main(String[] args) {
		// Load .env variables into environment
		Dotenv dotenv = Dotenv.configure().load();
		System.setProperty("AZURE_STORAGE_CONNECTION_STRING", dotenv.get("AZURE_STORAGE_CONNECTION_STRING"));
		System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));
		System.setProperty("GOOGLE_API_KEY", dotenv.get("GOOGLE_API_KEY"));
		SpringApplication.run(KltnApplication.class, args);
	}

}
