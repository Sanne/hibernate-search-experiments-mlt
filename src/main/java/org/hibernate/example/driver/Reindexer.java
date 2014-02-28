package org.hibernate.example.driver;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.example.model.Case;
import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.hibernate.search.impl.SimpleIndexingProgressMonitor;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;


public class Reindexer {

	public static void main(String[] args) throws InterruptedException {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory( "support" );
		try {
			final EntityManager entityManager = factory.createEntityManager();
			try {
				rebuildIndexes( entityManager );
			}
			finally {
				entityManager.close();
			}
		}
		finally {
			factory.close();
		}
	}

	private static void rebuildIndexes(EntityManager entityManager) throws InterruptedException {
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager( entityManager );
		MassIndexerProgressMonitor monitor = new SimpleIndexingProgressMonitor( 1000 );
		fullTextEntityManager.createIndexer( Case.class )
			.progressMonitor( monitor )
			.batchSizeToLoadObjects( 15 )
			.threadsToLoadObjects( 10 )
			.startAndWait();
	}

}
