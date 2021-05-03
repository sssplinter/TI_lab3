package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigInteger;

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

    final public static int ITER_COUNT = 5;

    @FXML
    void onEncipherClick() {
        cipherArea.clear();
        plainArea.clear();
        if (filePath != null) {
            int res = checkValue(valueN.getText(), valueP.getText(), valueQ.getText(), valueB.getText());
            if (res == 0) {
                StringBuilder input = new StringBuilder();
                StringBuilder output = new StringBuilder();
                fileEncrypt(filePath, new BigInteger(valueN.getText()), new BigInteger(valueB.getText()),
                        input, output);
                plainArea.setText(input.toString());
                cipherArea.setText(output.toString());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                String context;
                switch (res) {
                    case 1: {
                        context = "Не все поля заполнены.";
                        break;
                    }
                    case 2: {
                        context = "Не выполнено условие n > b.";
                        break;
                    }
                    case 3: {
                        context = "Не выполнено условие n = p * q.";
                        break;
                    }
                    case 4: {
                        context = "р не соответствует условию p = 3 mod 4";
                        break;
                    }
                    case 5: {
                        context = "q не соответствует условию q = 3 mod 4";
                        break;
                    }
                    case 6: {
                        context = "p не является простым числом.";
                        break;
                    }
                    case 7: {
                        context = "q не является простым числом.";
                        break;
                    }
                    case 9: {
                        context = "Ошибка преобразования текста в biginteger.";
                        break;
                    }

                    default:
                        throw new IllegalStateException("Unexpected value: " + res);
                }

                alert(context);

            }
        } else {
            alert("Файл не выбран.");
        }
    }

    @FXML
    void onDecipherClick() {
        cipherArea.clear();
        plainArea.clear();
        if (filePath != null) {
            if (checkValue(valueN.getText(), valueP.getText(), valueQ.getText(), valueB.getText()) == 0) {
                StringBuilder input = new StringBuilder();
                StringBuilder output = new StringBuilder();
                fileDecrypt(filePath, new BigInteger(valueP.getText()), new BigInteger(valueQ.getText()),
                        new BigInteger(valueB.getText()), input, output);
                plainArea.setText(input.toString());
                cipherArea.setText(output.toString());
            } else {
                //TODO
                alert("Incorrect values");
            }
        }
    }

    @FXML
    private void onGenerateBClick() {
        valueN.clear();
        valueB.clear();
        if (valueP.getText().length() == 0 || valueQ.getText().length() == 0) {
            alert("Для генерации n и b, поля p и q\nдолжны быть заполнены.");
        } else {
            BigInteger P = new BigInteger(valueP.getText());
            BigInteger Q = new BigInteger(valueQ.getText());
            if (checkPrivateKeys(P, Q)) {
                try {
                    BigInteger N = P.multiply(Q);
                    valueN.setText(N.toString());
                    BigInteger B = generateB(N);
                    valueB.setText(B.toString());
                } catch (Exception e) {
                    alert("Некорректные данные.");
                }
            }
        }
    }

    private boolean checkPrivateKeys(BigInteger P, BigInteger Q) {
        boolean isCorrect = true;
        if (!checkPrivateKey(P)) {
            alert("р не соответствует условию p = 3 mod 4");
            isCorrect = false;
        }

        if (!checkPrivateKey(Q)) {
            alert("q не соответствует условию q = 3 mod 4");
            isCorrect = false;
        }

        if (!millerRabinTest(P, ITER_COUNT)) {
            alert("p не является простым числом.");
            isCorrect = false;
        }

        if (!millerRabinTest(Q, ITER_COUNT)) {
            alert("q не является простым числом.");
            isCorrect = false;
        }
        return isCorrect;
    }

    private void alert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    @FXML
    public void openFile() {
        filePath = choseFileName();
        filePathText.setText(filePath);
        plainArea.clear();
        cipherArea.clear();
    }

    @FXML
    public void saveFile() {
        filePath = choseFileName();
    }

    private String choseFileName() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setTitle("Select Some File");
        fileChooser.setInitialDirectory(new File("D:\\tests\\test_lab2_TI"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            return file.getPath();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Ошибка выбора файла.");
            alert.setContentText("Файл не выбран.");
            alert.showAndWait();
            return null;
        }
    }
}
