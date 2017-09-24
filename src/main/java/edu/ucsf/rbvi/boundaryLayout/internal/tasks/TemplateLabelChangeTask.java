package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import javax.swing.JLabel;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.ui.TemplateThumbnailPanel;

public class TemplateLabelChangeTask extends AbstractTask {
	private JLabel templateLabel;
	private TemplateManager templateManager;
	private TemplateThumbnailPanel templatesPanel;

	@Tunable (description = "New name of template: ")
	public String newTemplateName = null;

	public TemplateLabelChangeTask(TemplateManager templateManager,
			TemplateThumbnailPanel templatesPanel, JLabel templateLabel) {
		super();
		this.templateManager = templateManager;
		this.templatesPanel = templatesPanel;
		this.templateLabel = templateLabel;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(newTemplateName != null) {
			String oldTemplateName = templateLabel.getText();
			if(!templateManager.renameTemplate(oldTemplateName, newTemplateName) || 
					!templatesPanel.renameTemplate(oldTemplateName, newTemplateName)) {
				System.out.println("did not rename properly!");
			}
			templateLabel.setText(newTemplateName);
			templateLabel.update(templateLabel.getGraphics());
		}
	}
}