package Application;

import Application.Labeling.LabelNum;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class scSystemLabelingView{
	
	private VBox labelingPane;
	private ListView<LabelNum> labelingView;
	private Label labelingText;
	
	private Labeling labeling;
	
	public scSystemLabelingView() {
		labeling = new Labeling();
		labelingPane = new VBox();
		labelingPane.setSpacing(20);
		labelingPane.setPadding(new Insets(10,10,10,10));
		labelingText = new Label("No signal");
		labelingView = new ListView<LabelNum>(
			FXCollections.observableArrayList(LabelNum.LEARNING, LabelNum.STOPPING, LabelNum.BORERING, LabelNum.NON_LABELING, LabelNum.ROBOT_TALKING)
		);
		labelingView.setPrefWidth(200);
		labelingView.setPrefHeight(250);
		labelingPane.getChildren().addAll(labelingText, labelingView);
	}
	
	public void setlabelingViewListener() {
		labelingView.getSelectionModel().selectedItemProperty().addListener(
	            (ObservableValue<? extends LabelNum> ov, LabelNum old_val, LabelNum new_val) -> {
                    labelingText.setText(new_val.toString());
                    labeling.setLabeling(new_val);
	    });		
	}
	
	public Pane getPane() {
		return labelingPane;
	}
	
	public Labeling getLabeling() {
		return labeling;
	}
	
	public void dispose() {
		
	}
}
