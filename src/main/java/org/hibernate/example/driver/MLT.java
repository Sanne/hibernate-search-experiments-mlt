package org.hibernate.example.driver;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.lucene.search.Query;
import org.hibernate.example.model.Case;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;


public class MLT {

	/** The STRESS_LOOPS */
	private static final int STRESS_LOOPS = 100000;

	/** The ID for a Case we want to find similar matches for */
	private static final String MODEL_CASE_ID = "00023630";

	private static final boolean STRESS = false;

	private static final boolean EXPLAIN_SCORE = true;//!STRESS;

	int queriesDone = 0;
	long startNanos = 0;

	/**
	 * -Xmx4G -Xms4G -XX:MaxPermSize=128M -XX:+HeapDumpOnOutOfMemoryError -Xss512k -XX:HeapDumpPath=/tmp/java_heap -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=127.0.0.1 -XX:+UseLargePages -XX:LargePageSizeInBytes=2m -Dlog4j.configuration=file:/opt/log4j.xml -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -da -XX:+AggressiveOpts
	 */
	public static void main(String[] args) throws InterruptedException {
		final MLT driver = new MLT();
		final EntityManagerFactory factory = Persistence.createEntityManagerFactory( "support" );
		try {
			final EntityManager entityManager = factory.createEntityManager();
			try {
				driver.stressMLT( Search.getFullTextEntityManager( entityManager ) );
			}
			finally {
				entityManager.close();
			}
		}
		finally {
			factory.close();
		}
	}

	private void stressMLT(FullTextEntityManager fullTextEntityManager) {
		if ( STRESS ) {
			startNanos = System.nanoTime();
			for (int i=0; i<STRESS_LOOPS; i++) {
				playWithMLT( fullTextEntityManager );
			}
		}
		else {
			playWithMLT( fullTextEntityManager );
		}
	}

	private void playWithMLT(FullTextEntityManager fullTextEntityManager) {
		QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity( Case.class ).get();

		Query query = queryBuilder.moreLikeThis().favorSignificantTermsWithFactor( 1.0f ).comparingAllFields().toEntityWithId( MODEL_CASE_ID ).createQuery();

		if ( ! STRESS ) {
			System.out.println( "Executing now: " + query.toString() );
		}
		FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery( query, Case.class );
		if ( STRESS || ! EXPLAIN_SCORE ) {
			//Avoid including EXPLANATION in this case as it's very unfair on Lucene's capabilities to run efficiently
			fullTextQuery.setProjection( FullTextQuery.SCORE, FullTextQuery.DOCUMENT_ID, "subject", FullTextQuery.THIS );
		}
		else {
			fullTextQuery.setProjection( FullTextQuery.SCORE, FullTextQuery.DOCUMENT_ID, "subject", FullTextQuery.THIS, FullTextQuery.EXPLANATION ); 
		}
		fullTextQuery.setMaxResults( 10 );
		List resultList = fullTextQuery.getResultList();
		if ( ! STRESS ) {
			for ( Object o : resultList ) {
				Object[] projection = (Object[]) o;
				System.out.println();
				System.out.println( " ---##########################################################################################################---" );
				System.out.println( " ---Score: " + projection[0] );
				if (EXPLAIN_SCORE) System.out.println( " ---Explanation: " + projection[4] );
				System.out.println( " ---Case: " + projection[1] + " - " + projection[2] );
				Case supportCase = (Case) projection[3];
				System.out.println( " ---Language: " + supportCase.getCase_language() );
				System.out.println( " ---Product: " + supportCase.getProduct() );
				System.out.println( " ---Sbr Groups: " + supportCase.getSbr_groups() );
				System.out.println( " ---Severity: " + supportCase.getSeverity() );
				System.out.println( " ---Tags: " + supportCase.getTags() );
				System.out.println( " ---Version: " + supportCase.getVersion() );
				System.out.println( " ---Description:\n" + supportCase.getDescription() );
			}
		}
		else {
			this.queriesDone++;
			if ( this.queriesDone == 100 ) {
				this.queriesDone = 0;
				long endNanos = System.nanoTime();
				long millisecs = TimeUnit.MILLISECONDS.convert( (endNanos - startNanos), TimeUnit.NANOSECONDS );
				System.out.println( "Done another 100! in " + millisecs );
				this.startNanos = endNanos;
			}
		}
	}


}
