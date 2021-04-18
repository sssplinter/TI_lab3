package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static application.RabinUtilsKt.*;


public class Controller {
    private String filePath = null;

    @FXML
    TextArea plainArea;

    @FXML
    TextArea cipherArea;

    @FXML
    Text filePathText;

    @FXML
    TextField valueN;

    @FXML
    TextField valueP;

    @FXML
    TextField valueQ;

    @FXML
    TextField valueB;

    @FXML
    void choseFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(filePathText.getScene().getWindow());
        if (file != null) {
            filePath = file.getPath();
            filePathText.setText(file.getName());
        }
    }

    @FXML
    void onEncipherClick() {
        if (testFile()){
            if (checkValue(valueN.getText(), valueP.getText(), valueQ.getText(), valueB.getText())){
                StringBuilder input = new StringBuilder();
                StringBuilder output = new StringBuilder();
                fileEncrypt(filePath, new BigInteger(valueN.getText()), new BigInteger(valueB.getText()),
                        input, output);
                plainArea.setText(input.toString());
                cipherArea.setText(output.toString());
            }else{
                alert("Incorrect values");
            }
        }
    }

    @FXML
    void onDecipherClick() {
        if (testFile()){
            if (checkValue(valueP.getText(), valueQ.getText(), valueB.getText())){
                StringBuilder input = new StringBuilder();
                StringBuilder output = new StringBuilder();
                fileDecrypt(filePath, new BigInteger(valueP.getText()), new BigInteger(valueQ.getText()),
                        new BigInteger(valueB.getText()), input, output);
//                plainArea.setText(input.toString());
                cipherArea.setText(output.toString());
            }else{
                alert("Incorrect values");
            }
        }
    }

    @FXML
    private void onGenerateBClick(){
        if (valueN.getText().length() == 0){
            alert("Incorrect N value");
        }else{
            try{
                BigInteger N = new BigInteger(valueN.getText());
                BigInteger B = generateB(N);
                valueB.setText(B.toString());
            }catch (Exception e){
                alert("Incorrect N value");
            }
        }
    }

    private boolean testFile() {
        if (filePath == null) {
            alert("Chose file for correct work.");
            return false;
        }
        return true;
    }

    private void alert(String msg){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

}
