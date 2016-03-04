/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system.scenes.components.mainstage.displays;

import org.pidome.client.system.scenes.windows.SimpleErrorMessage;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.pidome.client.config.AppResources;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.client.data.ClientSession;
import org.pidome.client.system.domotics.PidomeClients;
import org.pidome.client.system.domotics.components.messaging.AbstractMessagingMessage;
import org.pidome.client.system.domotics.components.messaging.ClientMessaging;
import org.pidome.client.system.domotics.components.messaging.ClientMessagingListener;
import org.pidome.client.system.scenes.components.controls.DefaultButton;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredList;
import org.pidome.client.system.scenes.components.mainstage.displays.components.TabbedContent;
import org.pidome.client.system.scenes.components.mainstage.displays.components.TabbedContentTabChangedListener;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredListItem;
import org.pidome.client.system.scenes.windows.TitledWindow;
import org.pidome.client.system.scenes.windows.WindowComponent;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John
 */
public class MessagesDisplay extends TitledWindow implements ClientMessagingListener, TabbedContentTabChangedListener {

    TabbedContent tabs = new TabbedContent();

    Label currentFrom = new Label();
    Label currentSubject = new Label();
    TextField readField = new TextField();

    DefaultButton replyButton = new DefaultButton("Reply");
    DefaultButton sendButton = new DefaultButton("Send");

    double widthRatio = DisplayConfig.getWidthRatio();
    double heightRatio = DisplayConfig.getHeightRatio();

    String currentTab;

    FilteredList list = new FilteredList("date");

    String currentMessageIndexSelected;

    VBox readPane;
    HBox display;

    public MessagesDisplay(String windowId, String windowName) {
        super(windowId, windowName);
        setId("messageswindow");
        list.setListSize(450, 300);
        readField.setPrefSize(425 * widthRatio, 140 * heightRatio);
        readField.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        readField.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        readField.setAlignment(Pos.TOP_LEFT);
        readField.getStyleClass().add("readfield");
        readField.setFocusTraversable(false);
        readField.setMouseTransparent(true);

        readField.setEditable(false);

        replyButton.setOnMouseClicked((MouseEvent mouseEvent) -> {
            fillReply(Integer.parseInt(currentMessageIndexSelected));
        });
    }

    @Override
    protected void setupContent() {
        list.build();
        tabs.addTabChangedListener(this);
        tabs.addTab("Notifications", "Notifications");
        tabs.addTab("System", "System");
        tabs.addTab("Clients", "Clients");
        setupDetailDisplay();
        setContent(setupDisplay());
    }

    @Override
    protected void removeContent() {
        list.destroy();
        tabs.removeTabChangedListener(this);
        ClientMessaging.removeClientMessagingListener(this);
    }

    final void setupDetailDisplay() {
        readPane = new VBox();
        VBox detailBox = new VBox(3);
        detailBox.getStyleClass().add("readdetailbox");
        detailBox.setPadding(new Insets(0, 0, 10, 0));
        Label from = new Label("From:");
        from.setMinWidth(50 * widthRatio);
        Label subj = new Label("Subject:");
        subj.setMinWidth(50 * widthRatio);
        detailBox.getChildren().addAll(new HBox(3) {
            {
                getChildren().addAll(from, currentFrom);
            }
        },
                new HBox(3) {
                    {
                        getChildren().addAll(subj, currentSubject);
                    }
                });
        readPane.getChildren().addAll(detailBox,
                readField,
                replyButton);

        readPane.setPadding(new Insets(10));
    }

    final HBox setupDisplay() {
        display = new HBox();
        display.getChildren().addAll(tabs);
        return display;
    }

    final void fillReply(int msgId) {
        switch (currentTab) {
            case "Clients":
                SendDisplay sendDisplay = new SendDisplay(this, "Reply message", "Reply message");
                sendDisplay.setReplyData(msgId);
                WindowManager.openWindow(sendDisplay);
                break;
        }
    }

    final void showMessageDetails(int msgId) {
        switch (currentTab) {
            case "Clients":
                currentFrom.setText(ClientMessaging.getMessageList().get(msgId).getFrom());
                currentSubject.setText("Client message");
                readField.setText(ClientMessaging.getMessageList().get(msgId).getMessage());
                list.highlightRow(currentMessageIndexSelected);
                currentMessageIndexSelected = String.valueOf(msgId);
                list.deHighlightRow(currentMessageIndexSelected);
                ClientMessaging.getMessageList().get(msgId).markRead(true);
                break;
        }
        if (!display.getChildren().contains(readPane)) {
            display.getChildren().add(readPane);
        }
    }

    @Override
    public void tabSwitched(String oldTab, String newTab) {
        ClientMessaging.removeClientMessagingListener(this);
        switch (newTab) {
            case "Clients":

                ImageView image = new ImageView(new Image(AppResources.getImage("icons/newClientMessage.png")));

                image.setOnMouseClicked((MouseEvent mouseEvent) -> {
                    SendDisplay sendDisplay = new SendDisplay(this, "New message", "New message");
                    WindowManager.openWindow(sendDisplay, mouseEvent.getSceneX(), mouseEvent.getSceneY());
                });

                HBox newMessage = new HBox(2);
                newMessage.getChildren().addAll(image, new Label("New message"));
                newMessage.setAlignment(Pos.CENTER_LEFT);

                VBox setConstruct = new VBox(3);
                setConstruct.setAlignment(Pos.CENTER_LEFT);
                setConstruct.setPadding(new Insets(4 * heightRatio, 0, 4 * heightRatio, 0));

                List<ClientMessaging.ClientMessage> msgList = ClientMessaging.getMessageList();
                for (int i = 0; i < msgList.size(); i++) {
                    list.addItem(createMessageListItem(i, msgList.get(i)));
                }

                setConstruct.getChildren().addAll(newMessage, list);
                tabs.setTabContent("Clients", setConstruct, "Content");
                ClientMessaging.addClientMessagingListener(this);
                break;
            case "System":
                Label na = new Label("Not available ye.\n\nImportant system messages will appear here, when available.");
                na.setPadding(new Insets(15));
                tabs.setTabContent("System", na, "N/A");
                break;
            case "Notifications":
                Label not = new Label("Not available ye.\n\nImportant system messages will appear here, when available.");
                not.setPadding(new Insets(15));
                tabs.setTabContent("Notifications", not, "N/A");
                break;
        }
        currentTab = newTab;
    }

    final FilteredListItem createMessageListItem(int id, AbstractMessagingMessage message) {
        FilteredListItem item = new FilteredListItem(String.valueOf(id), message.getDateTimeStringId(), "date" , message.getDate());
        item.setContent(constructMessage(id, message));
        return item;
    }

    @Override
    public void onChanged(final ListChangeListener.Change change) {
        while (change.next()) {
            if (change.wasRemoved() && change.wasAdded()) {
                for (int i = change.getFrom(); i < change.getTo(); i++) {
                    final int j = i;
                    Platform.runLater(() -> {
                        list.removeItem(String.valueOf(j));
                        list.addItem(createMessageListItem(j, (AbstractMessagingMessage) change.getList().get(j)));
                    });
                }
            } else if (change.wasRemoved()) {
                for (int i = change.getFrom(); i < change.getTo(); i++) {
                    final int j = i;
                    Platform.runLater(() -> {
                        list.removeItem(String.valueOf(j));
                    });
                }
            } else if (change.wasAdded()) {
                for (int i = change.getFrom(); i < change.getTo(); i++) {
                    final int j = i;
                    Platform.runLater(() -> {
                        list.addItem(createMessageListItem(j, (AbstractMessagingMessage) change.getList().get(j)));
                    });
                }
            }
        }
    }

    final HBox constructMessage(int msgId, AbstractMessagingMessage msg) {
        final HBox message = new HBox(4);
        message.getChildren().addAll(new Label(msg.getDate()), new Label(msg.getFrom()), new Label(msg.getSubject()));
        message.setUserData(msgId);
        message.setMinWidth(425 * widthRatio);
        message.setPadding(new Insets(5 * heightRatio));
        message.setOnMouseClicked((MouseEvent mouseEvent) -> {
            showMessageDetails((int) message.getUserData());
        });
        return message;
    }

    final class SendDisplay extends TitledWindow {

        boolean reply = false;

        TextField sendTo = new TextField();
        TextField newSubject = new TextField();
        TextField writeField = new TextField();

        ChoiceBox cb;

        ListChangeListener clientsList = this::clientListHelper;

        public SendDisplay(WindowComponent parent, String windowId, String windowName) {
            super(parent, windowId, windowName, 440, 290);

            writeField.setPrefSize(425 * widthRatio, 140 * heightRatio);
            writeField.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            writeField.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            writeField.setAlignment(Pos.TOP_LEFT);
            writeField.getStyleClass().add("writefield");

            sendButton.setOnMouseClicked((MouseEvent mouseEvent) -> {
                sendMessage();
                WindowManager.closeWindow(this);
            });

            sendTo.setEditable(false);
            newSubject.setEditable(false);
            writeField.setEditable(false);

            sendTo.setFocusTraversable(false);
            newSubject.setFocusTraversable(false);
            writeField.setFocusTraversable(false);

            cb = new ChoiceBox();
            for (int i = 0; i < PidomeClients.getClientList().size(); i++) {
                if(!PidomeClients.getClientList().get(i).equals(ClientSession.getClientName())){
                    cb.getItems().add(PidomeClients.getClientList().get(i));
                }
            }
            PidomeClients.getClientList().addListener(clientsList);
        }

        final void clientListHelper(Change<String> change) {
            while (change.next()) {
                if (change.wasRemoved() && change.wasAdded()) {
                    for (int i = change.getFrom(); i < change.getTo(); i++) {
                        final int j = i;
                        Platform.runLater(() -> {
                            cb.getItems().remove(change.getList().get(j));
                            cb.getItems().add(change.getList().get(j));
                        });
                    }
                } else if (change.wasRemoved()) {
                    for (int i = change.getFrom(); i < change.getTo(); i++) {
                        final int j = i;
                        Platform.runLater(() -> {
                            cb.getItems().remove(change.getList().get(j));
                        });
                    }
                } else if (change.wasAdded()) {
                    for (int i = change.getFrom(); i < change.getTo(); i++) {
                        final int j = i;
                        Platform.runLater(() -> {
                            cb.getItems().add(change.getList().get(j));
                        });
                    }
                }
            }
        }

        final void setReplyData(int msgId) {
            this.reply = true;
            sendTo.setText(ClientMessaging.getMessageList().get(msgId).getFrom());
            writeField.setEditable(true);
        }

        final void sendMessage() {
            switch (currentTab) {
                case "Clients":
                    if ((!sendTo.getText().isEmpty() || cb.getSelectionModel().isEmpty() == false) && !writeField.getText().isEmpty()) {
                        if (cb.getSelectionModel().isEmpty() == false) {
                            ClientMessaging.sendMessage((String) cb.getSelectionModel().getSelectedItem(), writeField.getText());
                        } else {
                            ClientMessaging.sendMessage(sendTo.getText(), writeField.getText());
                        }
                        sendTo.setText("");
                        writeField.setText("");
                        newSubject.setText("");
                        close(null);
                    } else {
                        SimpleErrorMessage windowNode = new SimpleErrorMessage("Send error");
                        windowNode.setMessage("Fill in all the fields");
                        WindowManager.openWindow(windowNode);
                    }
                    break;
            }
        }

        @Override
        protected void setupContent() {
            setContent(setupDisplay());
        }

        @Override
        protected void removeContent() {
            PidomeClients.getClientList().removeListener(clientsList);
        }

        final VBox setupDisplay() {
            VBox writePane = new VBox();
            VBox writeBox = new VBox(3);
            writeBox.getStyleClass().add("writedetailbox");
            writeBox.setPadding(new Insets(10, 0, 10, 0));
            writeBox.setAlignment(Pos.CENTER_LEFT);
            VBox.setMargin(sendButton, new Insets(5 * heightRatio, 0, 0, 0));
            Label to = new Label("To:");
            to.setAlignment(Pos.CENTER_LEFT);
            to.setMinWidth(50 * widthRatio);
            Label newsubj = new Label("Subject:");
            newsubj.setAlignment(Pos.CENTER_LEFT);
            newsubj.setMinWidth(50 * widthRatio);

            if (this.reply == false) {
                writeBox.getChildren().add(new HBox(3) {
                    {
                        getChildren().addAll(to, cb);
                    }
                });
            } else {
                writeBox.getChildren().add(new HBox(3) {
                    {
                        getChildren().addAll(to, sendTo);
                    }
                });
            }
            writeBox.getChildren().add(new HBox(3) {
                {
                    getChildren().addAll(newsubj, newSubject);
                }
            });
            newSubject.setText("Client message");
            writeField.setEditable(true);
            writePane.getChildren().addAll(writeBox,
                    writeField,
                    sendButton);
            writePane.setPadding(new Insets(5 * heightRatio, 10 * widthRatio, 5 * heightRatio, 10 * widthRatio));
            return writePane;
        }

    }

}
