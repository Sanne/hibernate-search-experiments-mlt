package org.hibernate.example.model;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2014-02-24T19:53:43.482+0000")
@StaticMetamodel(Case.class)
public class Case_ {
	public static volatile SingularAttribute<Case, String> casenumber;
	public static volatile SingularAttribute<Case, String> subject;
	public static volatile SingularAttribute<Case, String> description;
	public static volatile SingularAttribute<Case, String> product;
	public static volatile SingularAttribute<Case, String> version;
	public static volatile SingularAttribute<Case, Date> createddate;
	public static volatile SingularAttribute<Case, String> severity;
	public static volatile SingularAttribute<Case, String> case_language;
	public static volatile SingularAttribute<Case, String> tags;
	public static volatile SingularAttribute<Case, String> sbr_groups;
}
