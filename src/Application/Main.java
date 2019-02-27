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

			// 動画ファイルのパスを取得
			File f = new File(videoname+".mp4");

			// 動画再生クラスをインスタンス化
			Media Video = new Media(f.toURI().toString());
			MediaPlayer Play = new MediaPlayer(Video);
			MediaView mediaView = new MediaView(Play);
			mediaView.setFitWidth(600);
			root.setCenter(mediaView);

			// 画面下に表示する情報バーを作成
			HBox bottomNode = new HBox(10.0);
			bottomNode.getChildren().add(createButton(Play)); // 再生・停止・繰り返しボタン作成
			bottomNode.getChildren().add(createTimeSlider(Play)); // 時間表示スライダ作成
			bottomNode.getChildren().add(createVolumeSlider(Play)); // ボリューム表示スライダ作成
			root.setBottom(bottomNode);

			scSystemLabelingView labelingView = new scSystemLabelingView();
			Pane labelingPane = labelingView.getPane();
			labelingView.setlabelingViewListener();
			root.setRight(labelingPane);

			Scene scene = new Scene(root, 900, 500);

			primaryStage.setTitle("VideoPlay");
			primaryStage.setScene(scene);
			primaryStage.show();

			// 動画再生開始
			// Play.play();

			TreeMap<Double, Integer> timeMap = new TreeMap<>();
			TreeMap<Double, Integer> durationMap = new TreeMap<>();			
			JsonWriter writer = new JsonWriter(filename);
			// 動作確認用の出力を設定
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
				double currentTime = 0; // 0.5秒刻み
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
	 * 再生、一時停止、停止、連続再生ボタンを作成
	 * 
	 * @param mp
	 * @return
	 */
	public Node createButton(MediaPlayer mp) {
		// 表示コンポーネントを作成
		HBox root = new HBox(1.0);
		Button playButton = new Button("Play");
		Button doubleSpeedButton = new Button("×2 Play");
		Button pauseButton = new Button("Pause");
		Button stopButton = new Button("Stop");
		ToggleButton repeatButton = new ToggleButton("Repeat");
		root.getChildren().add(playButton);
		root.getChildren().add(doubleSpeedButton);
		root.getChildren().add(pauseButton);
		root.getChildren().add(stopButton);
		// root.getChildren().add( repeatButton );

		// 再生ボタンにイベントを登録
		EventHandler<ActionEvent> playHandler = (e) -> {
			// 再生開始
			mp.setRate(1.0);
			mp.play();
		};
		playButton.addEventHandler(ActionEvent.ACTION, playHandler);

		EventHandler<ActionEvent> doubleSpeedHandler = (e) -> {
			// 再生開始
			mp.setRate(2.0);
			mp.play();
		};
		doubleSpeedButton.addEventHandler(ActionEvent.ACTION, doubleSpeedHandler);

		// 一時停止ボタンにイベントを登録
		EventHandler<ActionEvent> pauseHandler = (e) -> {
			// 一時停止
			mp.pause();
		};
		pauseButton.addEventHandler(ActionEvent.ACTION, pauseHandler);

		// 停止ボタンにイベントを登録
		EventHandler<ActionEvent> stopHandler = (e) -> {
			// 停止
			mp.stop();
		};
		stopButton.addEventHandler(ActionEvent.ACTION, stopHandler);

		// 連続再生設定
		Runnable repeatFunc = () -> {
			// 連続再生ボタンの状態を取得し
			if (repeatButton.isSelected()) {
				// 頭だしして再生
				mp.seek(mp.getStartTime());
				mp.play();
			} else {
				// 頭だしして停止
				mp.seek(mp.getStartTime());
				mp.stop();
			}
			;
		};
		mp.setOnEndOfMedia(repeatFunc);

		return root;
	}

	/**
	 * 再生時間を表示・操作するスライダを作成
	 * 
	 * @param mp
	 * @return
	 */
	public Node createTimeSlider(MediaPlayer mp) {
		// 表示コンポーネントを作成
		HBox root = new HBox(5.0);
		Slider slider = new Slider();
		Label info = new Label();
		root.getChildren().add(slider);
		root.getChildren().add(info);

		// 再生準備完了時に各種情報を取得する関数を登録
		Runnable beforeFunc = mp.getOnReady(); // 現在のレディ関数
		Runnable readyFunc = () -> {
			// 先に登録された関数を実行
			if (beforeFunc != null) {
				beforeFunc.run();
			}

			// スライダの値を設定
			slider.setMin(mp.getStartTime().toSeconds());
			slider.setMax(mp.getStopTime().toSeconds());
			slider.setSnapToTicks(true);
		};
		mp.setOnReady(readyFunc);

		// 再生中にスライダを移動
		// プレイヤの現在時間が変更されるたびに呼び出されるリスナを登録
		ChangeListener<? super Duration> playListener = (ov, old, current) -> {
			// 動画の情報をラベル出力
			String infoStr = String.format("%4.2f", mp.getCurrentTime().toSeconds()) + "/"
					+ String.format("%4.2f", mp.getTotalDuration().toSeconds());
			info.setText(infoStr);

			// スライダを移動
			slider.setValue(mp.getCurrentTime().toSeconds());
		};
		mp.currentTimeProperty().addListener(playListener);

		// スライダを操作するとシークする
		EventHandler<MouseEvent> sliderHandler = (e) -> {
			// スライダを操作すると、シークする
			mp.seek(javafx.util.Duration.seconds(slider.getValue()));

		};
		slider.addEventFilter(MouseEvent.MOUSE_RELEASED, sliderHandler);

		return root;
	}

	/**
	 * ボリュームを表示・操作するスライダを作成
	 * 
	 * @param mp
	 * @return
	 */
	public Node createVolumeSlider(MediaPlayer mp) {
		// 表示コンポーネントを作成
		HBox root = new HBox(5.0);
		Label info = new Label();
		Slider slider = new Slider();
		root.getChildren().add(info);
		root.getChildren().add(slider);

		// 再生準備完了時に各種情報を取得する関数を登録
		Runnable beforeFunc = mp.getOnReady(); // 現在のレディ関数
		Runnable readyFunc = () -> {
			// 先に登録された関数を実行
			if (beforeFunc != null) {
				beforeFunc.run();
			}

			// スライダの値を設定
			slider.setMin(0.0);
			slider.setMax(1.0);
			slider.setValue(mp.getVolume());
		};
		mp.setOnReady(readyFunc);

		// 再生中にボリュームを表示
		// プレイヤの現在時間が変更されるたびに呼び出されるリスナを登録
		ChangeListener<? super Number> sliderListener = (ov, old, current) -> {
			// 動画の情報をラベル出力
			String infoStr = String.format("Vol:%4.2f", mp.getVolume());
			info.setText(infoStr);

			// スライダにあわせてボリュームを変更
			mp.setVolume(slider.getValue());

		};
		slider.valueProperty().addListener(sliderListener);

		return root;
	}

}