package org.hibernate.example.tests;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.junit.rules.ExternalResource;

public class TestEntityManager extends ExternalResource {

	private EntityManagerFactory entityManagerFactory;
	private FullTextEntityManager fullTextEntityManager;

	@Override
	protected void before() throws Throwable {
		entityManagerFactory = Persistence.createEntityManagerFactory( "in-memory" );
	}

	@Override
	protected void after() {
		try {
			if ( fullTextEntityManager != null ) {
				fullTextEntityManager.close();
			}
		}
		finally {
			entityManagerFactory.close();
		}
	}

	public FullTextEntityManager getEntityManager() {
		if ( fullTextEntityManager == null ) {
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			fullTextEntityManager = Search.getFullTextEntityManager( entityManager );
		}
		return fullTextEntityManager;
	}

	public SearchFactory getSearchFactory() {
		return getEntityManager().getSearchFactory();
	}

}
