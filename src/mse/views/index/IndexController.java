package mse.views.index;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Label;
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
    @FXML
    Label indexNameLabel;

    /**
     * Initialise the view by showing all the tokens in the hymn index
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // create initial instances of logger, config and index store
        logger = new Logger(LogLevel.DEBUG);
        logger.openLog();
        cfg = new Config(logger);
        indexStore = new IndexStore(cfg);

        // start by showing hymn index
        AuthorIndex hymnIndex = indexStore.getIndex(logger, Author.HYMNS);
        indexNameLabel.setText(Author.HYMNS.getName());

        // show all tokens for hymn book
        IndexTree.setRoot(getRootForAllTokens(hymnIndex));

        logger.closeLog();
    }

    /**
     * Choose a specific token to show the references for
     * Can be left blank to show all references for that author
     *
     * @param e
     */
    @FXML
    public void choose(ActionEvent e) {
        logger.openLog();

        // get and log the token to show
        String token = tokenField.getText();
        logger.log(LogLevel.INFO, "Token: " + token);

        // get the index to search (todo add more indexes)
        AuthorIndex hymnIndex = indexStore.getIndex(logger, Author.HYMNS);

        if (token.equals("")) {
            IndexTree.setRoot(getRootForAllTokens(hymnIndex));
        } else {
            IndexTree.setRoot(getTreeItemForSingleToken(hymnIndex, token));
        }

        logger.closeLog();
    }

    /**
     * Get the tree item of all the tokens in an author index, with their count
     *
     * @param index Author index to display
     * @return All tokens in the author index and their count in a tree item
     */
    private TreeItem<String> getRootForAllTokens(AuthorIndex index) {
        TreeItem<String> rootItem = new TreeItem<>("Indexes");
        rootItem.setExpanded(true);
        for (Map.Entry<String, Integer> tokenCountEntry : index.getTokenCountMap().entrySet()) {
            rootItem.getChildren().add(new TreeItem<>(tokenCountEntry.getKey() + " : " + tokenCountEntry.getValue()));
        }
        return rootItem;
    }

    /**
     * Get the tree item to be displayed that shows where the references of a single token are found
     *
     * @param index Author index that contains references to the token
     * @param token Token to display references for
     * @return The tree item of the token and it's references
     */
    private TreeItem<String> getTreeItemForSingleToken(AuthorIndex index, String token) {
        TreeItem<String> rootItem = new TreeItem<>("Indexes");
        rootItem.setExpanded(true);
        for (Map.Entry<String, Integer> tokenCountEntry : index.getTokenCountMap().entrySet()) {
            if (tokenCountEntry.getKey().equalsIgnoreCase(token)) {
                TreeItem<String> references = new TreeItem<>(tokenCountEntry.getKey() + " : " + tokenCountEntry.getValue());
                short[] refs = index.getReferences(tokenCountEntry.getKey());
                int count = 0;
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
        return rootItem;
    }
}
