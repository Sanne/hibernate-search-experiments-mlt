package org.hibernate.example.model;

import java.io.Serializable;
import java.lang.String;
import java.util.Date;

import javax.persistence.*;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TermVector;

@Entity
@Table(name="cases")
@Indexed(index="cases")
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
	public String getCasenumber() {
		return this.casenumber;
	}

	public void setCasenumber(String casenumber) {
		this.casenumber = casenumber;
	}

	@Lob @Field(termVector=TermVector.YES, store=Store.YES)
	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Lob @Field(termVector=TermVector.YES)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Field(termVector=TermVector.YES)
	public String getProduct() {
		return this.product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	@Field(termVector=TermVector.YES)
	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Field(termVector=TermVector.YES)
	public Date getCreateddate() {
		return this.createddate;
	}

	public void setCreateddate(Date createddate) {
		this.createddate = createddate;
	}

	/* Ignored for now as there's some problem in fetching this column -> didn't investigate yet
	@Field
	public Date getCloseddate() {
		return this.closeddate;
	}

	public void setCloseddate(Date closeddate) {
		this.closeddate = closeddate;
	}
	*/

	@Field(termVector=TermVector.YES)
	public String getSeverity() {
		return this.severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	@Field(termVector=TermVector.YES)
	public String getCase_language() {
		return this.case_language;
	}

	public void setCase_language(String case_language) {
		this.case_language = case_language;
	}

	@Field(termVector=TermVector.YES)
	@Column(columnDefinition="text")
	public String getTags() {
		return this.tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@Field(termVector=TermVector.YES)
	@Column(columnDefinition="text")
	public String getSbr_groups() {
		return this.sbr_groups;
	}

	public void setSbr_groups(String sbr_groups) {
		this.sbr_groups = sbr_groups;
	}

}
