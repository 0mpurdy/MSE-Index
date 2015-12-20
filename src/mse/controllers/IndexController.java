package mse.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import mse.common.LogLevel;
import mse.common.Logger;
import mse.data.Author;
import mse.data.AuthorIndex;
import mse.data.HymnBook;
import mse.data.IndexStore;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import mse.common.*;

public class IndexController implements Initializable {

    Logger logger;
    Config cfg;
    IndexStore indexStore;

    @FXML
    TreeView<String> IndexTree;
    @FXML
    TextField tokenField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        logger = new Logger(LogLevel.DEBUG);
        logger.openLog();
        cfg = new Config(logger);
        indexStore = new IndexStore(cfg);

        AuthorIndex hymnIndex = indexStore.getIndex(logger, Author.HYMNS);

        TreeItem<String> rootItem = new TreeItem<>("Indexes");
        rootItem.setExpanded(true);
        for (Map.Entry<String, Integer> tokenCountEntry : hymnIndex.getTokenCountMap().entrySet()) {
            rootItem.getChildren().add(new TreeItem<>(tokenCountEntry.getKey() + " : " + tokenCountEntry.getValue()));
        }
        IndexTree.setRoot(rootItem);

        logger.closeLog();
    }

    @FXML
    public void choose(ActionEvent e) {
        logger.openLog();

        String token = tokenField.getText();
        logger.log(LogLevel.INFO, "Token: " + token);

        AuthorIndex hymnIndex = indexStore.getIndex(logger, Author.HYMNS);

        TreeItem<String> rootItem = new TreeItem<>("Indexes");
        rootItem.setExpanded(true);
        for (Map.Entry<String, Integer> tokenCountEntry : hymnIndex.getTokenCountMap().entrySet()) {
            if (tokenCountEntry.getKey().equalsIgnoreCase(token)) {
                TreeItem<String> references = new TreeItem<>(tokenCountEntry.getKey() + " : " + tokenCountEntry.getValue());
                short[] refs = hymnIndex.getReferences(tokenCountEntry.getKey());
                int count =0;
                int cVol;
                int cRef;
                TreeItem<String> vol = null;
                while (count < refs.length) {
                    cRef = refs[count];

                    if (cRef < 0) {
                        if (vol != null) references.getChildren().add(vol);
                        cVol = -cRef;
                        vol = new TreeItem<>(HymnBook.values()[cVol - 1].getName());
                    } else {
                        if (vol != null) vol.getChildren().add(new TreeItem<>(Integer.toString(cRef)));
                    }

                    count++;
                }
                if (vol != null) references.getChildren().add(vol);
                rootItem.getChildren().add(references);
            }
        }
        IndexTree.setRoot(rootItem);

        logger.closeLog();
    }
}
