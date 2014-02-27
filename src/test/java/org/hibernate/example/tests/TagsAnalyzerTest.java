package org.hibernate.example.tests;

import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.hibernate.example.model.Case;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.junit.Rule;
import org.junit.Test;

public class TagsAnalyzerTest {

	String[] tagFields = { 	"##",
							"#Hibernate#", //wrong case on purpose
							"#hibernate#hibernate_search#", //note we don't want to filter the underscore
							"#hibernate#hibernate_search#",
							"#hibernate#hibernate_search#InfiniSpan#", //wrong case on purpose
							"#RHel 7#", //Respect spaces
							"##",
							"#jon#",
							"#RHel 7#jon#infinispan#" };

	@Rule
	public TestEntityManager emf = new TestEntityManager();

	@Test
	public void tagsAnalysis() throws IOException {
		FullTextEntityManager em = emf.getEntityManager();
		storeSomeStuff( em );
		SearchFactory searchFactory = emf.getSearchFactory();
		try ( IndexReader indexReader = searchFactory.getIndexReaderAccessor().open( Case.class ) ) {
			Assert.assertEquals( 0, indexReader.docFreq( new Term( "tags", "##" ) ) );
			Assert.assertEquals( 4, indexReader.docFreq( new Term( "tags", "hibernate" ) ) );
			Assert.assertEquals( 3, indexReader.docFreq( new Term( "tags", "hibernate_search" ) ) );
			Assert.assertEquals( 2, indexReader.docFreq( new Term( "tags", "infinispan" ) ) );
			Assert.assertEquals( 2, indexReader.docFreq( new Term( "tags", "rhel 7" ) ) );
			Assert.assertEquals( 2, indexReader.docFreq( new Term( "tags", "jon" ) ) );
		}
	}

	private void storeSomeStuff(FullTextEntityManager em) {
		Date date = new Date();
		em.getTransaction().begin();
		for (int i=0; i<tagFields.length; i++) {
			Case supportCase = new Case();
			supportCase.setCasenumber( "000" + i );
			supportCase.setCase_language( "EN" );
			supportCase.setDescription( "description number " +i );
			supportCase.setCreateddate( date );
			supportCase.setProduct( "product" );
			supportCase.setSbr_groups( "group" );
			supportCase.setSeverity( "critical" );
			supportCase.setSubject( "Subject " + i );
			supportCase.setTags( tagFields[i] );
			supportCase.setVersion( "1" );
			em.persist( supportCase );
		}
		em.getTransaction().commit();
		em.clear();
	}

}
