package com.util;
import java.math.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.alchemyapi.api.*;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
public class Utility {

	
	public static void main(String[] args) {
		PrintWriter jsonWriter;
		
		JSONParser parser = new JSONParser();
		String str;
		String lang = "";
		int i=0;
		String cliendId = ""; 			//add your clientID
		String cliendSecret = "";		//add your clientSecret
		//JSONArray jsonarray = new JSONArray();

		try {	 
			Language fromLanguage = Language.ENGLISH;
			Language toLanguage = Language.ENGLISH;
			Object obj = parser.parse(new FileReader("train.json"));
			String text="";
			String translated_text="";
			JSONArray jsonarray = (JSONArray) obj;
			
			Location location = null;
			Tags taxonomySet = null;
			Tags entitySet = null; 
			
			Set<String> contentTags = null;
			Set<String> entityHierarchy = null; 
			
			for (Object object : jsonarray) {	
				i++;
				JSONObject jsonObj = (JSONObject)object;
				
				lang = (String)jsonObj.get("lang");
				if(!lang.equals("en")){
					jsonObj.put("text_en", "");
				}else{
					fromLanguage=Language.ENGLISH;
					text=(String)jsonObj.get("text_en");
				}
				if(!lang.equals("fr")){
					jsonObj.put("text_fr", "");
				}else {
					fromLanguage=Language.FRENCH;
					text=(String)jsonObj.get("text_fr");
				}
				if(!lang.equals("es")){
					jsonObj.put("text_es", "");
				}else {
					fromLanguage=Language.SPANISH;
					text=(String)jsonObj.get("text_es");
				}
				if(!lang.equals("ru")){
					jsonObj.put("text_ru", "");
				}else {
					fromLanguage=Language.RUSSIAN;
					text=(String)jsonObj.get("text_ru");
				}
				if(!lang.equals("de")){
					jsonObj.put("text_de", "");
				}else {
					fromLanguage=Language.GERMAN;
					text=(String)jsonObj.get("text_de");
				}
				
				jsonObj.remove("text_replace");
				jsonObj.remove("location");	
				
				jsonObj.put("text", text);
				
				translated_text = getTranslatedText(text, fromLanguage, toLanguage, cliendId, cliendSecret );
				jsonObj.put("translated_text", translated_text);			
				
				contentTags = new TreeSet<String>();
				entitySet = getEntity(translated_text);
				taxonomySet = getTaxonomy(translated_text);
				contentTags.addAll(entitySet.getContentTagsSet());
				contentTags.addAll(taxonomySet.getContentTagsSet());
				jsonObj.put("content_tags", contentTags);
								
				entityHierarchy = new TreeSet<String>();
				entityHierarchy.addAll(entitySet.getEntityHierarchySet());
				entityHierarchy.addAll(taxonomySet.getEntityHierarchySet());
				jsonObj.put("entity_hierarchy", entityHierarchy);				
				
				location = getLocation();
				jsonObj.put("latitude", location.getLatitude());
				jsonObj.put("longitude", location.getLongitude());
				
			}
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");	
			String fileName="training_data_"+lang+"_"+(sdf.format(date).replace('/', '_')+".json");
			jsonWriter = new PrintWriter(fileName, "UTF-8");
			jsonWriter.write(jsonarray.toJSONString());
			jsonWriter.flush();
			jsonWriter.close();
			
			System.out.println("json file generated with name : " + fileName);
			System.out.println(jsonarray.toJSONString());

		}catch(Exception e){
			e.printStackTrace();
		}

		String csvFile = "Book1.csv";
		BufferedReader br = null;
	}

	
	public static String getTranslatedText(String text, Language fromLang, Language toLang, String clientId, String clientSecret)  {
		// Set your Windows Azure Marketplace client info - See http://msdn.microsoft.com/en-us/library/hh454950.aspx
		Translate.setClientId(clientId);
		Translate.setClientSecret(clientSecret);
		String translatedVersion="";
		try{
			//System.out.println("text : "+text);
			//System.out.println("fromlang : " +fromLang.toString()+ " to lang :" +toLang.toString());
			translatedVersion = Translate.execute(text, fromLang, toLang);
		}catch(Exception e){
			e.printStackTrace();
		}
		String translatedText = translatedVersion.replaceAll("\n", " ").replaceAll("\"", "") ;

		return translatedText;
	}

	public static Location getLocation() {

		String csvFile = "Book1.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		String retVal = "";
		long random = new Date().getTime();
		Random r = new Random(random);
		Location location = new Location();



		int modulus = r.nextInt()%465;
		if(modulus<0)
			modulus+=465;

		int i =0;

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {				
				String[] locationArray = line.split(cvsSplitBy);
				location.setLatitude(locationArray[2]);
				location.setLongitude(locationArray[3]);				
				if(i==modulus)
					break;
				i++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return location;
	}

	public static Tags getTaxonomy(String text){

		Tags taxonomySet = new Tags();
		try{
			BufferedReader br = null;
			String line ="";

			AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile("api_key.txt");

			StringBuffer contentTags = new StringBuffer();
			StringBuffer entityHierarchy;			

			Document doc = alchemyObj.TextGetTaxonomy(text);
			String str = getStringFromDocument(doc);
			InputStream is = new ByteArrayInputStream(str.getBytes());
			br = new BufferedReader(new InputStreamReader(is));

			//br = new BufferedReader(new FileReader("F:\\project\\sample.txt"));
			while ((line = br.readLine()) != null) {

				entityHierarchy = new StringBuffer();
				if(line.contains("label")){					
					String taxonomyString[] = line.replaceAll("</", "<").split("<label>");					
					String taxonomy[] = taxonomyString[1].split("/");
					for (int i=1;i<taxonomy.length;i++) {

						taxonomySet.getContentTagsSet().add("\"" + taxonomy[i] + "\"");
						if(taxonomy.length>2 && i<3){
							entityHierarchy.append(taxonomy[i]).append(":");							
						}
					}

					if(entityHierarchy!=null && entityHierarchy.length()>0){
						taxonomySet.getEntityHierarchySet().add("\"" + entityHierarchy.toString().substring(0, entityHierarchy.toString().length()-1) + "\"");					
					}
				}
			}	

			br.close();

		}
		catch(Exception e){
			e.printStackTrace();
		}
		return taxonomySet;
	}

	private static Tags getEntity(String text){


		Tags taxonomySet = new Tags();
		try{
			BufferedReader br = null;
			String line ="";
			boolean isEntity=false;

			AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile("api_key.txt");

			StringBuffer contentTags = new StringBuffer();
			StringBuffer entityHierarchy = new StringBuffer();			

			Document doc = alchemyObj.TextGetRankedNamedEntities(text);
			String str = getStringFromDocument(doc);
			InputStream is = new ByteArrayInputStream(str.getBytes());
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				if(line.contains("<entity>")){
					entityHierarchy = new StringBuffer();
					isEntity=true;
				}
				if(isEntity){

					if(line.contains("</entity>")){
						isEntity = false;
						continue;
					}
					if(line.contains("<type>")){
						String typeString[] = line.replaceAll("</", "<").split("<type>");
						taxonomySet.getContentTagsSet().add("\"" + typeString[1] + "\"");
						entityHierarchy.append(typeString[1]);						
					}
					if(line.contains("<subType>")){
						String subTypeString[] = line.replaceAll("</", "<").split("<subType>");
						entityHierarchy.append(":" + subTypeString[1]);
						taxonomySet.getEntityHierarchySet().add("\""+entityHierarchy.toString()+"\"");						
						isEntity=false;
					}					
				}
			}				
			br.close();

		}
		catch(Exception e){
			e.printStackTrace();
		}
		return taxonomySet;
	}

	private static String getStringFromDocument(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);

			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}



}

class Location{
	private String latitude;
	private String longitude;
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
}

class Tags{
	private Set<String> contentTagsSet = new TreeSet<String>();
	private Set<String> entityHierarchySet = new TreeSet<String>();
	public Set<String> getContentTagsSet() {
		return contentTagsSet;
	}
	public void setContentTagsSet(Set<String> contentTagsSet) {
		this.contentTagsSet = contentTagsSet;
	}
	public Set<String> getEntityHierarchySet() {
		return entityHierarchySet;
	}
	public void setEntityHierarchySet(Set<String> entityHierarchySet) {
		this.entityHierarchySet = entityHierarchySet;
	}
	@Override
	public String toString() {
		return "TaxonomySet [contentTagsSet=" + contentTagsSet + ", entityHierarchySet=" + entityHierarchySet + "]";
	}


}
