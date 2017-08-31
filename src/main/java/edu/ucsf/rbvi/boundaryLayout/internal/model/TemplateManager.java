package edu.ucsf.rbvi.boundaryLayout.internal.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

public class TemplateManager {
	private Map<String, List<String>> templates;
	private final CyServiceRegistrar registrar;
	public static final String NETWORK_TEMPLATES = "Templates Applied";

	public TemplateManager(CyServiceRegistrar registrar) {
		templates = new HashMap<>();
		this.registrar = registrar;
	}

	public boolean addTemplate(String templateName, 
			List<Annotation> annotations) {
		if(templates.containsKey(templateName))
			return overwriteTemplate(templateName, annotations);
		List<String> annotationsInfo = 
				getAnnotationInformation(annotations);
		templates.put(templateName, annotationsInfo);
		if(templates.containsKey(templateName)) 
			return true;
		return false;
	}

	public boolean deleteTemplate(String templateName) {
		if(!templates.containsKey(templateName))
			return false;
		templates.remove(templateName);
		if(!templates.containsKey(templateName))
			return true;
		return false;
	}

	public boolean overwriteTemplate(String templateName, 
			List<Annotation> annotations) {
		if(!templates.containsKey(templateName))
			return false;
		templates.replace(templateName, 
				getAnnotationInformation(annotations));
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean useTemplate(String templateName, 
			CyNetworkView networkView) {
		if(!templates.containsKey(templateName))
			return false;
		AnnotationFactory<Annotation> shapeFactory = 
				registrar.getService(AnnotationFactory.class);
		AnnotationManager annotationManager = registrar.getService(
				AnnotationManager.class);	
		List<String> templateInformation = templates.get(templateName);

		for(String annotationInformation : templateInformation) {
			String[] argsArray = annotationInformation.substring(
					annotationInformation.indexOf(')') + 3, 
					annotationInformation.length() - 1).split(", ");
			Map<String, String> argMap = new HashMap<>();
			for(String arg : argsArray) {
				String[] keyValuePair = arg.split("=");
				System.out.println(keyValuePair[0] + "=" + keyValuePair[1]);
				argMap.put(keyValuePair[0], keyValuePair[1]);
			}
			Annotation addedShape = shapeFactory.createAnnotation(
					Annotation.class, networkView, argMap);
			addedShape.setName(argMap.get(Annotation.NAME));
			annotationManager.addAnnotation(addedShape);
			addedShape.update();
		}
		CyTable networkTable = networkView.getModel().getDefaultNetworkTable();
		networkTable.createListColumn("Templates Applied", String.class, false);
		
		networkView.updateView();
		return true;
	}
	
	public boolean importTemplate(String templateName,
			File templateFile) throws IOException {
		if(!templateFile.exists())
			return false;
		BufferedReader templateReader = new BufferedReader(
				new FileReader(templateFile.getAbsolutePath()));
		List<String> templateInformation = new ArrayList<>();
		String annotationInformation = "";
		while((annotationInformation = templateReader.readLine()) 
				!= null)
			templateInformation.add(annotationInformation);
		if(!templates.containsKey(templateName))
			templates.put(templateName, templateInformation);
		else
			templates.replace(templateName, templateInformation);
		try {
			templateReader.close();
		} catch (IOException e) {
			throw new IOException("Problems writing to stream: " + 
					templateReader.toString() + 
					"[" + e.getMessage()+ "]");
		}
		if(templates.containsKey(templateName))
			return true;
		return false;
	}

	public boolean exportTemplate(String templateName, 
			String absoluteFilePath) throws IOException {
		if(!templates.containsKey(templateName))
			return false;
		File exportedFile = new File(absoluteFilePath);
		if(!exportedFile.exists())
			exportedFile.createNewFile();
		BufferedWriter templateWriter = new 
				BufferedWriter(new FileWriter(exportedFile));
		for(String annotationInformation : templates.get(templateName)) {
			//MODIFY WHAT IS EXPORTED - CURRENTLY EXPORTING
			//RAW DATA
			templateWriter.write(annotationInformation);
		}
		try {
			templateWriter.close();
		} catch (IOException e) {
			throw new IOException("Problems writing to stream: " + 
					templateWriter.toString() + 
					"[" + e.getMessage()+ "]");
		}
		return true;
	}
	
	public void networkRemoveTemplates(CyNetworkView networkView, 
			List<String> templateRemoveNames) {
		AnnotationManager annotationManager = registrar.
				getService(AnnotationManager.class);
		List<Annotation> annotations = annotationManager.
				getAnnotations(networkView);
		List<String> uuidsToRemove = new ArrayList<>();
		for(String templateRemoveName : templateRemoveNames) {
			if(templates.containsKey(templateRemoveName)) {
				List<String> templateInformation =
						templates.get(templateRemoveName);
				for(String annotationInformation : templateInformation) {
					String[] argsArray = annotationInformation.substring(
							annotationInformation.indexOf(')') + 3, 
							annotationInformation.length() - 1).split(", ");
					Map<String, String> argMap = new HashMap<>();
					for(String arg : argsArray) {
						String[] keyValuePair = arg.split("=");
						argMap.put(keyValuePair[0], keyValuePair[1]);
					}
					uuidsToRemove.add(argMap.get("uuid"));
				}
			}
		}
		for(Annotation annotation : annotations)
			if(uuidsToRemove.contains(annotation.getUUID()))
				annotationManager.removeAnnotation(annotation);
		networkView.updateView();
	}
	
	private static List<String> getAnnotationInformation(
			List<Annotation> annotations) {
		List<String> annotationsInfo = new ArrayList<>();
		for(Annotation annotation : annotations)
			annotationsInfo.add(annotation.getArgMap().toString());
		return annotationsInfo;
	}

	public List<String> getTemplateNames() {
		return new ArrayList<>(templates.keySet());
	}
}