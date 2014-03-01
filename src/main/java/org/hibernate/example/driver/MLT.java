package org.hibernate.example.driver;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.Query;
import org.hibernate.example.model.Case;
import org.hibernate.search.exception.AssertionFailure;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.MoreLikeThisContext;
import org.hibernate.search.query.dsl.QueryBuilder;


public class MLT {

	/** The FIELD_NAMES */
	private static final String[] FIELD_NAMES = new String[]{ "subject", "ngrams_subject", "ngrams_description", "description", "product", "case_language", "tags", "sbr_groups", "product_versioned" };

	/** The STRESS_LOOPS */
	private static final int STRESS_LOOPS = 100000;

	/** The ID for a Case we want to find similar matches for */
	private static final String MODEL_CASE_ID = "00023644";

	private static final boolean STRESS = false;

	private static final boolean EXPLAIN_SCORE = false;//!STRESS;

	private static final boolean TRADITIONAL_IMPLEMENTATION = true;

	private static final boolean USE_TERM_BOOSTING = true;

	int queriesDone = 0;
	long startNanos = 0;

	/**
	 * -Xmx4G -Xms4G -XX:MaxPermSize=128M -XX:+HeapDumpOnOutOfMemoryError -Xss512k -XX:HeapDumpPath=/tmp/java_heap -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=127.0.0.1 -XX:+UseLargePages -XX:LargePageSizeInBytes=2m -Dlog4j.configuration=file:/opt/log4j.xml -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -da -XX:+AggressiveOpts
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
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

	private void stressMLT(FullTextEntityManager fullTextEntityManager) throws IOException {
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

	private void playWithMLT(FullTextEntityManager fullTextEntityManager) throws IOException {

		Query query = TRADITIONAL_IMPLEMENTATION ? buildLuceneTraditionalQuery( fullTextEntityManager ) : buildHibernateSearchQuery( fullTextEntityManager );

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

	private Query buildLuceneTraditionalQuery(FullTextEntityManager fullTextEntityManager) throws IOException {
		QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity( Case.class ).get();
		Query idQuery = queryBuilder.keyword().onField( "casenumber" ).ignoreFieldBridge().matching( MODEL_CASE_ID ).createQuery();
		FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery( idQuery, Case.class ).setProjection( FullTextQuery.DOCUMENT_ID, FullTextQuery.ID );
		Object[] results = (Object[])fullTextQuery.getSingleResult();
		Integer docId = (Integer) results[0];
		String loadedDocumentId = (String) results[1];
		if ( ! MODEL_CASE_ID.equals( loadedDocumentId ) ) {
			throw new AssertionFailure( "error" );
		}

		try ( IndexReader reader = fullTextEntityManager.getSearchFactory().getIndexReaderAccessor().open( Case.class ) ) {
			MoreLikeThis mlt = new MoreLikeThis( reader );
			mlt.setFieldNames( FIELD_NAMES );
			mlt.setBoost( USE_TERM_BOOSTING );
			mlt.setMinTermFreq( 1 );
			mlt.setMinWordLen( 2 );
			return mlt.like( docId.intValue() );
		}
	}

	private Query buildHibernateSearchQuery(FullTextEntityManager fullTextEntityManager) {
		QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity( Case.class ).get();
		MoreLikeThisContext moreLikeThis = queryBuilder.moreLikeThis();
		if ( USE_TERM_BOOSTING ) {
			moreLikeThis.boostedTo( 1.0f );
		}
		return moreLikeThis
				.comparingFields( FIELD_NAMES )
				.toEntityWithId( MODEL_CASE_ID )
				.createQuery();
	}


}
