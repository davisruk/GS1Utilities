package com.boots.gs1.service.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.Data;

@Configuration
@ConfigurationProperties("gs1")

public class GS1BuilderParameters {
	
	private Map<String, GS1BuildParameter> parameters = new HashMap<>();
	public Map<String, GS1BuildParameter> getParameters() { return parameters; }
	public void setParameters(Map <String, GS1BuildParameter> parameters  ) { this.parameters = parameters; }
	public List<ParameterRange> ranges = new ArrayList<>();
	public void setRanges (List<ParameterRange> ranges) { this.ranges = ranges; }
	public List<ParameterRange> getRanges() { return ranges; }
	
	private final int TWO_DIGIT_IDENTIFIER = 2;
	private final int THREE_DIGIT_IDENTIFIER = 3;
	private final int FOUR_DIGIT_IDENTIFIER = 4;
	
	@Data
	public static class GS1BuildParameter implements Cloneable{
		private String identifier;
		private int identifierLength;
		private boolean fixedLength;
		private int payloadLength;
		private String setterMethod;
		
		public void setIdentifier(String i) {
			identifier = i;
			identifierLength = i.length();
		}
		
		public Object clone () throws CloneNotSupportedException {
			GS1BuildParameter p = (GS1BuildParameter) super.clone();
			p.setIdentifier(identifier);
			p.setFixedLength(fixedLength);
			p.setPayloadLength(payloadLength);
			p.setSetterMethod(setterMethod);
			return p;
		}
		
		public static GS1BuildParameter instanceFrom(GS1BuildParameter template) throws CloneNotSupportedException {
			return (GS1BuildParameter) template.clone();
		}
	}
	
	@Data
	public static class ParameterRange {
		private int start;
		private int end;
		private GS1BuildParameter template;
	}
	
	public void addIdentifier() {
		this.parameters.forEach((k,v) -> v.setIdentifier(k));
	}
	
	public void expandRanges() {
		for (ParameterRange pr: this.ranges) {
			try {
				for (int i = pr.getStart(); i <= pr.getEnd(); i++) {
					pr.template.setIdentifier(""+ i);
					parameters.put("" + i, (GS1BuildParameter) pr.template.clone());
				}
				
			} catch (CloneNotSupportedException cnse) {
				System.out.println ("Clone Not Supported");
			}
		}
	}
	
	@PostConstruct
	public void setIdentifiersAndRanges() {
		addIdentifier();
		expandRanges();
		parameters.forEach((k,v) -> System.out.println(v));
	}
	
	public GS1BuildParameter getParameter (int startIndex, String barcode) throws IllegalArgumentException {
		GS1BuildParameter result;
	    String fieldCode = barcode.substring(startIndex, startIndex + TWO_DIGIT_IDENTIFIER);
	    result = parameters.get(fieldCode);
	    if (result == null) {
	    	fieldCode = barcode.substring(startIndex, startIndex + THREE_DIGIT_IDENTIFIER);
	    	result = parameters.get(fieldCode);
	    	if (result == null) {
		    	fieldCode = barcode.substring(startIndex, startIndex + FOUR_DIGIT_IDENTIFIER);
		    	result = parameters.get(fieldCode);
		    	if (result == null) {
		    		throw new IllegalArgumentException ("Unknown Identifier - " + fieldCode);
		    	}
	    	}
	    }
	    return result;
	}
}
