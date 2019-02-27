package Application;

import java.io.File;
import java.util.Iterator;
import java.util.TreeMap;

import Application.Labeling.LabelNum;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
	private String videoname = "video\\2019-02-22_16-41-10";
	private String filename = videoname+"_1";
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			BorderPane root = new BorderPane();

			// ����t�@�C���̃p�X���擾
			File f = new File(videoname+".mp4");

			// ����Đ��N���X���C���X�^���X��
			Media Video = new Media(f.toURI().toString());
			MediaPlayer Play = new MediaPlayer(Video);
			MediaView mediaView = new MediaView(Play);
			mediaView.setFitWidth(600);
			root.setCenter(mediaView);

			// ��ʉ��ɕ\��������o�[���쐬
			HBox bottomNode = new HBox(10.0);
			bottomNode.getChildren().add(createButton(Play)); // �Đ��E��~�E�J��Ԃ��{�^���쐬
			bottomNode.getChildren().add(createTimeSlider(Play)); // ���ԕ\���X���C�_�쐬
			bottomNode.getChildren().add(createVolumeSlider(Play)); // �{�����[���\���X���C�_�쐬
			root.setBottom(bottomNode);

			scSystemLabelingView labelingView = new scSystemLabelingView();
			Pane labelingPane = labelingView.getPane();
			labelingView.setlabelingViewListener();
			root.setRight(labelingPane);

			Scene scene = new Scene(root, 900, 500);

			primaryStage.setTitle("VideoPlay");
			primaryStage.setScene(scene);
			primaryStage.show();

			// ����Đ��J�n
			// Play.play();

			TreeMap<Double, Integer> timeMap = new TreeMap<>();
			TreeMap<Double, Integer> durationMap = new TreeMap<>();			
			JsonWriter writer = new JsonWriter(filename);
			// ����m�F�p�̏o�͂�ݒ�
			Play.currentTimeProperty().addListener((ov) -> {
				//System.out.println(Play.getCurrentTime());
				//System.out.println(labelingView.getLabeling().getLabeling());
				timeMap.put((double) Math.round(Play.getCurrentTime().toSeconds() * 10) / 10,
						labelingView.getLabeling().getLabeling());
			});
			Play.statusProperty().addListener((ov) -> {
				System.out.println(Play.getStatus());
				//System.out.println(timeMap);

				writer.writerOpen();
				Iterator<Double> it = timeMap.keySet().iterator();
				double previousTime =0;
				double currentTime = 0; // 0.5�b����
				int currentLabel = LabelNum.getLabelNumber(LabelNum.NON_LABELING);
				int[] labels = new int[5];
				while (it.hasNext()) {
					double key = it.next();
					if(key - currentTime >= 0.5) {
						int maxCount = labels[currentLabel];
						for (int i = 0; i < labels.length; i++) {
							if(maxCount < labels[i]) {
								currentLabel = i;
							};
							labels[i] = 0;
						}
						//currentLabel = (countInDuration != 0)?(int) Math.round(labelSum / countInDuration):LabelNum.getLabelNumber(LabelNum.NON_LABELING);
						durationMap.put(currentTime, currentLabel);
						
						if(currentTime - previousTime >= 1.0) {
							int count = (int) Math.floor((key - previousTime) / 0.5);
							for (int i = 1; i < count; i++) {
								int prevLabel = durationMap.get(previousTime);
								int currLabel = durationMap.get(currentTime);
								durationMap.put(previousTime+i*0.5, (int)Math.round(prevLabel+(currLabel-prevLabel)*i/count));
								writer.write(previousTime+i*0.5, (int)Math.round(prevLabel+(currLabel-prevLabel)*i/count));
							}
						}
						writer.write(currentTime, currentLabel);
						previousTime = currentTime;
						currentTime = Math.floor(key);
						if(key - currentTime > 0.5)currentTime += 0.5;					
					}
					labels[timeMap.get(key)]++;
					currentLabel = LabelNum.getLabelNumber(LabelNum.NON_LABELING);

					//System.out.println(key + "/" + timeMap.get(key));
				}
				//currentLabel = (countInDuration != 0)?(int) Math.round(labelSum / countInDuration):LabelNum.getLabelNumber(LabelNum.NON_LABELING);
				//writer.write(currentTime, currentLabel);
				writer.writeComment(timeMap.toString().substring(1, timeMap.toString().length()-1));
				//System.out.println(durationMap);
				int[] count = new int[5];
				for (Double key : durationMap.keySet()) {
					count[durationMap.get(key)]++;
				}
				System.out.println(LabelNum.getType(0)+" : "+count[0]*0.5);
				System.out.println(LabelNum.getType(1)+" : "+count[1]*0.5);
				System.out.println(LabelNum.getType(2)+" : "+count[2]*0.5);
				System.out.println(LabelNum.getType(3)+" : "+count[3]*0.5);
				writer.writeComment(LabelNum.getType(0)+" : "+count[0]*0.5+", "+LabelNum.getType(1)+" : "+count[1]*0.5+", "+LabelNum.getType(2)+" : "+count[2]*0.5+", "+LabelNum.getType(3)+" : "+count[3]*0.5);
				writer.writerClose();
			});
			

		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
	

	/**
	 * �Đ��A�ꎞ��~�A��~�A�A���Đ��{�^�����쐬
	 * 
	 * @param mp
	 * @return
	 */
	public Node createButton(MediaPlayer mp) {
		// �\���R���|�[�l���g���쐬
		HBox root = new HBox(1.0);
		Button playButton = new Button("Play");
		Button doubleSpeedButton = new Button("�~2 Play");
		Button pauseButton = new Button("Pause");
		Button stopButton = new Button("Stop");
		ToggleButton repeatButton = new ToggleButton("Repeat");
		root.getChildren().add(playButton);
		root.getChildren().add(doubleSpeedButton);
		root.getChildren().add(pauseButton);
		root.getChildren().add(stopButton);
		// root.getChildren().add( repeatButton );

		// �Đ��{�^���ɃC�x���g��o�^
		EventHandler<ActionEvent> playHandler = (e) -> {
			// �Đ��J�n
			mp.setRate(1.0);
			mp.play();
		};
		playButton.addEventHandler(ActionEvent.ACTION, playHandler);

		EventHandler<ActionEvent> doubleSpeedHandler = (e) -> {
			// �Đ��J�n
			mp.setRate(2.0);
			mp.play();
		};
		doubleSpeedButton.addEventHandler(ActionEvent.ACTION, doubleSpeedHandler);

		// �ꎞ��~�{�^���ɃC�x���g��o�^
		EventHandler<ActionEvent> pauseHandler = (e) -> {
			// �ꎞ��~
			mp.pause();
		};
		pauseButton.addEventHandler(ActionEvent.ACTION, pauseHandler);

		// ��~�{�^���ɃC�x���g��o�^
		EventHandler<ActionEvent> stopHandler = (e) -> {
			// ��~
			mp.stop();
		};
		stopButton.addEventHandler(ActionEvent.ACTION, stopHandler);

		// �A���Đ��ݒ�
		Runnable repeatFunc = () -> {
			// �A���Đ��{�^���̏�Ԃ��擾��
			if (repeatButton.isSelected()) {
				// ���������čĐ�
				mp.seek(mp.getStartTime());
				mp.play();
			} else {
				// ���������Ē�~
				mp.seek(mp.getStartTime());
				mp.stop();
			}
			;
		};
		mp.setOnEndOfMedia(repeatFunc);

		return root;
	}

	/**
	 * �Đ����Ԃ�\���E���삷��X���C�_���쐬
	 * 
	 * @param mp
	 * @return
	 */
	public Node createTimeSlider(MediaPlayer mp) {
		// �\���R���|�[�l���g���쐬
		HBox root = new HBox(5.0);
		Slider slider = new Slider();
		Label info = new Label();
		root.getChildren().add(slider);
		root.getChildren().add(info);

		// �Đ������������Ɋe������擾����֐���o�^
		Runnable beforeFunc = mp.getOnReady(); // ���݂̃��f�B�֐�
		Runnable readyFunc = () -> {
			// ��ɓo�^���ꂽ�֐������s
			if (beforeFunc != null) {
				beforeFunc.run();
			}

			// �X���C�_�̒l��ݒ�
			slider.setMin(mp.getStartTime().toSeconds());
			slider.setMax(mp.getStopTime().toSeconds());
			slider.setSnapToTicks(true);
		};
		mp.setOnReady(readyFunc);

		// �Đ����ɃX���C�_���ړ�
		// �v���C���̌��ݎ��Ԃ��ύX����邽�тɌĂяo����郊�X�i��o�^
		ChangeListener<? super Duration> playListener = (ov, old, current) -> {
			// ����̏������x���o��
			String infoStr = String.format("%4.2f", mp.getCurrentTime().toSeconds()) + "/"
					+ String.format("%4.2f", mp.getTotalDuration().toSeconds());
			info.setText(infoStr);

			// �X���C�_���ړ�
			slider.setValue(mp.getCurrentTime().toSeconds());
		};
		mp.currentTimeProperty().addListener(playListener);

		// �X���C�_�𑀍삷��ƃV�[�N����
		EventHandler<MouseEvent> sliderHandler = (e) -> {
			// �X���C�_�𑀍삷��ƁA�V�[�N����
			mp.seek(javafx.util.Duration.seconds(slider.getValue()));

		};
		slider.addEventFilter(MouseEvent.MOUSE_RELEASED, sliderHandler);

		return root;
	}

	/**
	 * �{�����[����\���E���삷��X���C�_���쐬
	 * 
	 * @param mp
	 * @return
	 */
	public Node createVolumeSlider(MediaPlayer mp) {
		// �\���R���|�[�l���g���쐬
		HBox root = new HBox(5.0);
		Label info = new Label();
		Slider slider = new Slider();
		root.getChildren().add(info);
		root.getChildren().add(slider);

		// �Đ������������Ɋe������擾����֐���o�^
		Runnable beforeFunc = mp.getOnReady(); // ���݂̃��f�B�֐�
		Runnable readyFunc = () -> {
			// ��ɓo�^���ꂽ�֐������s
			if (beforeFunc != null) {
				beforeFunc.run();
			}

			// �X���C�_�̒l��ݒ�
			slider.setMin(0.0);
			slider.setMax(1.0);
			slider.setValue(mp.getVolume());
		};
		mp.setOnReady(readyFunc);

		// �Đ����Ƀ{�����[����\��
		// �v���C���̌��ݎ��Ԃ��ύX����邽�тɌĂяo����郊�X�i��o�^
		ChangeListener<? super Number> sliderListener = (ov, old, current) -> {
			// ����̏������x���o��
			String infoStr = String.format("Vol:%4.2f", mp.getVolume());
			info.setText(infoStr);

			// �X���C�_�ɂ��킹�ă{�����[����ύX
			mp.setVolume(slider.getValue());

		};
		slider.valueProperty().addListener(sliderListener);

		return root;
	}

}