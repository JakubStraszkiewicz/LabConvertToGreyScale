/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package laborka3;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;


import java.io.*;
import static java.lang.Compiler.enable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javax.swing.JFileChooser;

/**
 *
 * @author Jakub
 */
public class FXMLDocumentController{
    
    @FXML
    public TableView<ConvertToGrayscale> convertTable;
    @FXML
    public TableColumn<ConvertToGrayscale, String> nameColumn;
    @FXML
    public TableColumn<ConvertToGrayscale, Double> progressColumn;
    @FXML
    public TableColumn<ConvertToGrayscale, String> statusColumn;
    @FXML
    public ToggleButton directoryButton;
    @FXML
    public ToggleButton fileButton;
    @FXML
    public RadioButton sekwencyjnieButton;
    @FXML
    public RadioButton wspolbieznieButton;
    @FXML
    public TextField textField;
    @FXML 
    public ToggleButton startButton;
    
    final String DEFAULT = "ilość wątków";

    public List<File> selectedFiles;
    
    ForkJoinPool forkJoinPool;
        
    public File directory;
    ObservableList<ConvertToGrayscale> jobs = FXCollections.observableArrayList();
    
    public void wspolbiezneToggle(MouseEvent event){
        sekwencyjnieButton.setSelected(false);
        textField.setDisable(false);
    }
    public void sekwencyjneToggle(MouseEvent event){
        wspolbieznieButton.setSelected(false);
        textField.setDisable(true);
    }
    
    public void directoryToggle(MouseEvent event){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directory = directoryChooser.showDialog(null);
       
    }
    public void startToggle(MouseEvent event){
        new Thread(this::backgroundJob).start();
        
    }
    
    private void backgroundJob(){
        for(ConvertToGrayscale job: jobs)
            job.status.setValue(job.WAITING);
        
        if(wspolbieznieButton.isSelected())
            if(directory != null) {
                if(textField.getText().equals(this.DEFAULT))
                    forkJoinPool = new ForkJoinPool(ForkJoinPool.getCommonPoolParallelism());
                else
                    forkJoinPool = new ForkJoinPool(Integer.parseInt(textField.getText()));
                 forkJoinPool.submit(this::convertParallel);
            }
        if(sekwencyjnieButton.isSelected())
        {
            long start = System.currentTimeMillis();
            jobs.stream().forEach(this::convertSequentional);
            long end = System.currentTimeMillis(); //czas po zakończeniu operacji [ms]
            long duration = end-start; //czas przetwarzania [ms]
            System.out.print((double)(duration/1000.0));
        }

        
}
    private void convertSequentional(ConvertToGrayscale job){
        
        job.convert(job.getFile(), this.directory, job.getProgressProperty());
          
    }
    
    private void convertParallel(){
        long start = System.currentTimeMillis();
        jobs.parallelStream().forEach(this::convertSequentional);
        long end = System.currentTimeMillis();
        long duration = end-start;
        System.out.print((duration/1000.0));
        
    }

    
    public void fileToggle(MouseEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG images", "*.jpg"));
        selectedFiles = fileChooser.showOpenMultipleDialog(null);
        for(File file: selectedFiles){
            if(selectedFiles!=null)
                jobs.add(new ConvertToGrayscale(file));
        }
        convertTable.setItems(jobs);
    }
    
    /*@FXML
    public ToggleButton chooseButton;
    @FXML
    public ToggleButton clientButton;
    @FXML
    public ToggleButton serverButton;
    @FXML
    public Label statusLabel;
    @FXML
    private ProgressBar progressBar;
            
    private static final int PORT = 1337;

    private ExecutorService executor = Executors.newFixedThreadPool(4);
    
    private File file;
        
    private StringProperty statusMsg = new SimpleStringProperty(STATUS_CONNECT);
    private static String STATUS_CONNECT = "Uruchom serwer lub połącz jako klient";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    
    public void serverToggle(MouseEvent event) throws IOException, ClassNotFoundException {
       Thread thread = new Thread(new Server());
       thread.start();
    }
    
    public void clientToggle(MouseEvent event) throws IOException, ClassNotFoundException {
       chooseButton.setDisable(false);
    }
    
    public void chooseToggle(MouseEvent event) throws IOException{
        JFileChooser fileChooser = new JFileChooser();
        progressBar.setDisable(false);
        fileChooser.showOpenDialog(null);
        file = fileChooser.getSelectedFile();
        Socket socket = new Socket("127.0.0.1",PORT);
        Task<Void> sendFileTask = new SendFileTask(file,socket);
        statusLabel.textProperty().bind(sendFileTask.messageProperty());
        progressBar.progressProperty().bind(sendFileTask.progressProperty());
        executor.submit(sendFileTask);
    }*/

    public void initialize() {
        nameColumn.setCellValueFactory( p -> new SimpleStringProperty(p.getValue().getFile().getName()));
        statusColumn.setCellValueFactory( p -> p.getValue().getStatusProperty());
        progressColumn.setCellFactory( ProgressBarTableCell.<ConvertToGrayscale>forTableColumn());
        progressColumn.setCellValueFactory( p -> p.getValue().getProgressProperty().asObject());

        
    }

    
    
   
}
