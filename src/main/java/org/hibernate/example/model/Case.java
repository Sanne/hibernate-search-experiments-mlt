package org.hibernate.example.model;

import java.io.Serializable;
import java.lang.String;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.pattern.PatternTokenizerFactory;
import org.apache.lucene.analysis.miscellaneous.KeywordMarkerFilterFactory;
import org.apache.lucene.analysis.ngram.NGramTokenizerFactory;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.AnalyzerDefs;
import org.hibernate.search.annotations.AnalyzerDiscriminator;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.TokenizerDef;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TermVector;

@Entity
@Table(name="cases")
@Indexed(index="cases")
@AnalyzerDefs({
	@AnalyzerDef(name="tags",
			tokenizer = @TokenizerDef(factory = PatternTokenizerFactory.class,
			params = {
				@Parameter(name = "pattern", value = "#?([^#]+)#"),
				@Parameter(name = "group", value = "1") } ),
			filters = {
			@TokenFilterDef(factory = ASCIIFoldingFilterFactory.class),
			@TokenFilterDef(factory = LowerCaseFilterFactory.class)
	}),
	@AnalyzerDef(name = "en",
		tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
			filters = {
				@TokenFilterDef(factory = LowerCaseFilterFactory.class),
				@TokenFilterDef(factory = KeywordMarkerFilterFactory.class,
					params = { @Parameter(name="protected", value="techkeywords.txt") }),
				@TokenFilterDef(factory = org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory.class
			)
		}),
	@AnalyzerDef(name = "de",
		tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
		filters = {
			@TokenFilterDef(factory = LowerCaseFilterFactory.class),
			@TokenFilterDef(factory = KeywordMarkerFilterFactory.class,
			params = { @Parameter(name="protected", value="techkeywords.txt") }),
			@TokenFilterDef(factory = org.apache.lucene.analysis.de.GermanStemFilterFactory.class)
		}),
	@AnalyzerDef(name = "neutral",
	tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
	filters = {
		@TokenFilterDef(factory = LowerCaseFilterFactory.class),
		@TokenFilterDef(factory = KeywordMarkerFilterFactory.class,
		params = { @Parameter(name="protected", value="techkeywords.txt") }),
		@TokenFilterDef(factory = org.apache.lucene.analysis.en.EnglishPossessiveFilterFactory.class)
	}),
	@AnalyzerDef(name = "ngrams",
		tokenizer = @TokenizerDef(factory = NGramTokenizerFactory.class,
		params = {
			@Parameter(name="minGramSize", value="3"),
			@Parameter(name="maxGramSize", value="4") }
		),
		filters = {
			@TokenFilterDef(factory = LowerCaseFilterFactory.class)
		})
})
public class Case implements Serializable {

	private String casenumber;
	private String subject;
	private String description;
	private String product;
	private String version;
	private Date createddate;
//	private Date closeddate;
	private String severity;
	private String case_language;
	private String tags;
	private String sbr_groups;
	private static final long serialVersionUID = 1L;

	public Case() {
		super();
	}

	@Id
	public String getCasenumber() { return this.casenumber; }
	public void setCasenumber(String casenumber) { this.casenumber = casenumber; }

	@Lob
	@Fields({
		@Field(termVector=TermVector.WITH_POSITION_OFFSETS, store=Store.YES, norms=Norms.YES, boost=@Boost(1.3f)),
		@Field(name="ngrams_subject",termVector=TermVector.WITH_POSITION_OFFSETS, norms=Norms.NO)
	})
	public String getSubject() { return this.subject; }
	public void setSubject(String subject) { this.subject = subject; }

	@Lob
	@Fields({
		@Field(termVector=TermVector.WITH_POSITION_OFFSETS, store=Store.NO, norms=Norms.YES, boost=@Boost(1.3f)),
		@Field(name="ngrams_description",termVector=TermVector.WITH_POSITION_OFFSETS, norms=Norms.NO)
	})
	public String getDescription() { return this.description; }
	public void setDescription(String description) { this.description = description; }

	@Field(termVector=TermVector.YES, norms=Norms.NO, analyze=Analyze.NO, boost=@Boost(1.2f))
	public String getProduct() { return this.product; }
	public void setProduct(String product) { this.product = product; }

	public String getVersion() { return this.version; }
	public void setVersion(String version) { this.version = version; }

	@Field(name="product_versioned", termVector=TermVector.YES, norms=Norms.NO, analyze=Analyze.NO, boost=@Boost(0.9f))
	@Transient//Not a database field
	public String getVersionProduct() {
		//Build a single identifying term for the Version of the product: we need to include the product to not match
		//the same version of different products
		return getProduct() + " " + getVersion();
	}

	public Date getCreateddate() { return this.createddate; }
	public void setCreateddate(Date createddate) { this.createddate = createddate; }

	public String getSeverity() { return this.severity; }
	public void setSeverity(String severity) { this.severity = severity; }

	@Field(termVector=TermVector.YES, norms=Norms.NO, analyze=Analyze.NO, boost=@Boost(0.5f))
	@AnalyzerDiscriminator(impl = LanguageDiscriminator.class)
	public String getCase_language() { return this.case_language; }
	public void setCase_language(String case_language) { this.case_language = case_language; }

	@Field(termVector=TermVector.YES, index = Index.YES, name = "tags")
	@Analyzer(definition="tags")
	@Column(columnDefinition="text")
	public String getTags() { return this.tags; }
	public void setTags(String tags) { this.tags = tags; }

	@Field(termVector=TermVector.YES)
	@Column(columnDefinition="text")
	public String getSbr_groups() { return this.sbr_groups; }
	public void setSbr_groups(String sbr_groups) { this.sbr_groups = sbr_groups; }

}
