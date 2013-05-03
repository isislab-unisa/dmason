package dmason.annotation;

public class AnnotationValue 
{
	private String domain;
	private String suggestedValue;
	
	public AnnotationValue(String name, String value) {
		super();
		this.domain = name;
		this.suggestedValue = value;
	}
	
	public String getDomain() {
		return domain;
	}
	public void setDomain(String name) {
		this.domain = name;
	}
	
	public String getSuggestedValue() {
		return suggestedValue;
	}
	public void setSuggestedValue(String value) {
		this.suggestedValue = value;
	}
	
}
