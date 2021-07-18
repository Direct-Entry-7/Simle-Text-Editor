package controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import util.FXUtil;

import java.awt.event.TextListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class EditorFormController {
    private final List<Index> searchList = new ArrayList<>();
    public TextArea txtEditor;
    public AnchorPane pneFind;
    public TextField txtSearch;
    public AnchorPane pneReplace;
    public TextField txtSearch1;        // This is inside the pneReplace
    public TextField txtReplace;
    public AnchorPane pneTextEditor;
    private int findOffset = -1;
    private PrinterJob printerJob;
    private File file;

    public void initialize() {
        pneFind.setVisible(false);
        pneReplace.setVisible(false);
        this.printerJob = PrinterJob.createPrinterJob();

        ChangeListener textListener = (ChangeListener<String>) (observable, oldValue, newValue) -> {
            searchMatches(newValue);
        };

        txtSearch.textProperty().addListener(textListener);
        txtSearch1.textProperty().addListener(textListener);


        txtEditor.setOnDragDone(event -> {
            System.out.println(event.getPickResult());
        });

    }

    private void searchMatches(String query){
        FXUtil.highlightOnTextArea(txtEditor,query, Color.web("yellow", 0.8));

        try {
            Pattern regExp = Pattern.compile(query);
            Matcher matcher = regExp.matcher(txtEditor.getText());

            searchList.clear();

            while (matcher.find()) {
                searchList.add(new Index(matcher.start(), matcher.end()));
            }

            if (searchList.isEmpty()){
                findOffset = -1;
            }
        } catch (PatternSyntaxException e) {

        }
    }

    public void mnuItemNew_OnAction(ActionEvent actionEvent) {
        txtEditor.clear();
        txtEditor.requestFocus();
        this.file = null;
    }

    public void mnuItemExit_OnAction(ActionEvent actionEvent) {
        Stage stage = (Stage)pneTextEditor.getScene().getWindow();
        stage.close();
    }

    public void mnuItemFind_OnAction(ActionEvent actionEvent) {
        findOffset = -1;
        if (pneReplace.isVisible()){
            pneReplace.setVisible(false);
        }
        pneFind.setVisible(true);
        txtSearch.requestFocus();
    }

    public void mnuItemReplace_OnAction(ActionEvent actionEvent) {
        findOffset = -1;
        if (pneFind.isVisible()){
            pneFind.setVisible(false);
        }
        pneReplace.setVisible(true);
        txtSearch1.requestFocus();
    }

    public void mnuItemSelectAll_OnAction(ActionEvent actionEvent) {
        txtEditor.selectAll();
    }

    public void btnFindNext_OnAction(ActionEvent actionEvent) {
        if (!searchList.isEmpty()) {
            findOffset++;
            if (findOffset >= searchList.size()) {
                findOffset = 0;
            }
            txtEditor.selectRange(searchList.get(findOffset).startingIndex, searchList.get(findOffset).endIndex);
        }
    }

    public void btnFindPrevious_OnAction(ActionEvent actionEvent) {
        if (!searchList.isEmpty()) {
            findOffset--;
            if (findOffset < 0) {
                findOffset = searchList.size() - 1;
            }
            txtEditor.selectRange(searchList.get(findOffset).startingIndex, searchList.get(findOffset).endIndex);
        }
    }

    public void btnReplaceAll_OnAction(ActionEvent actionEvent) {
        while (!searchList.isEmpty()) {
            txtEditor.replaceText(searchList.get(0).startingIndex, searchList.get(0).endIndex, txtReplace.getText());
            searchMatches(txtSearch1.getText());
        }
    }

    public void btnReplace_OnAction(ActionEvent actionEvent) {
        if (findOffset == -1) return;
        txtEditor.replaceText(searchList.get(findOffset).startingIndex, searchList.get(findOffset).endIndex, txtReplace.getText());
        searchMatches(txtSearch1.getText());
    }


    public void btnFindClose_OnMouseClicked(MouseEvent mouseEvent) {
        pneFind.setVisible(false);
    }

    public void btnReplaceClose_OnMouseClicked(MouseEvent mouseEvent) {
        pneReplace.setVisible(false);
    }

    public void mnuItemOpen_OnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add
                (new FileChooser.ExtensionFilter("All Text Files", "*.txt", "*.html"));
        fileChooser.getExtensionFilters().add
                (new FileChooser.ExtensionFilter("All Files", "*"));
        this.file = fileChooser.showOpenDialog(txtEditor.getScene().getWindow());

        if (file == null) return;

       openFile(file);

    }

    public void mnuItemSave_OnAction(ActionEvent actionEvent) {
        if (this.file == null) {
            mnuItemSaveAs_OnAction(new ActionEvent());
        }else{
            try (FileWriter fw = new FileWriter(this.file);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(txtEditor.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public void mnuItemSaveAs_OnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        File file = fileChooser.showSaveDialog(txtEditor.getScene().getWindow());

        if (file == null) return;

        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(txtEditor.getText());
            this.file = file;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mnuItemCut_OnAction(ActionEvent actionEvent) {
        txtEditor.cut();
    }

    public void mnuItemCopy_OnAction(ActionEvent actionEvent) {
        txtEditor.copy();
    }

    public void mnuItemPaste_OnAction(ActionEvent actionEvent) {
        txtEditor.paste();
    }

    public void mnuItemAbout_OnAction(ActionEvent actionEvent) {
    }

    public void mnuItemPageSetup_OnAction(ActionEvent actionEvent) {
        System.out.println("Called");
        this.printerJob.showPageSetupDialog(txtEditor.getScene().getWindow());
    }

    public void mnuItemPrint_OnAction(ActionEvent actionEvent) {
        boolean printDialog = printerJob.showPrintDialog(txtEditor.getScene().getWindow());

        if(printDialog){
            printerJob.printPage(txtEditor.lookup("Text"));
        }
    }

    public void txtEditor_OnMouseDragOver(MouseDragEvent mouseDragEvent) {
//        if(d)
    }

    public void txtEditor_OnDragOver(DragEvent dragEvent) {
        if(dragEvent.getDragboard().hasFiles()){
            dragEvent.acceptTransferModes(TransferMode.ANY);
            dragEvent.consume();
        }
    }

    public void txtEditor_OnDragDropped(DragEvent dragEvent) {
        if(dragEvent.getDragboard().hasFiles()){
            File file = dragEvent.getDragboard().getFiles().get(0);
            openFile(file);
            dragEvent.setDropCompleted(true);
        }
    }

    private void openFile(File file) {
        txtEditor.clear();
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                txtEditor.appendText(line + '\n');
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Index {
    int startingIndex;
    int endIndex;

    public Index(int startingIndex, int endIndex) {
        this.startingIndex = startingIndex;
        this.endIndex = endIndex;
    }
}

