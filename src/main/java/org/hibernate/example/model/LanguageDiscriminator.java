package org.hibernate.example.model;

import org.hibernate.search.analyzer.Discriminator;

/**
 * LanguageDiscriminator: chose the correct analyzer to be used for this Case instance.
 * NOTE: remember while tuning analyzers that one goal is to make sure Case instances from
 * different languages can be matched against each other, as long as no specific language filter
 * is enabled.
 */
public class LanguageDiscriminator implements Discriminator {

	public String getAnalyzerDefinitionName(Object value, Object entity, final String fieldName) {
		if ( value == null || !( entity instanceof Case ) ) {
			return null;
		}
		if ( "tags".equals( fieldName ) ) {
			return "tags";
		}
		final String languageCode = (String) value;
		if ( "en".equals( languageCode ) ) {
			return "en";
		}
		if ( "de".equals( languageCode ) ) {
			return "de";
		}
		return "neutral"; 
	}

}
