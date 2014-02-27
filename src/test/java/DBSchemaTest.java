import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

public class DBSchemaTest {

	@Test
	public void schemaValidation() {
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory( "support" );
	}

}
