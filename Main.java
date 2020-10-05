/*
 * File: Main.java
 * Authors: John Budnick & Naciur Bedoui
 * Status: Complete
 * Purpose: Creates a storefront that allows users to select and buy items, and allows admins to
 * update categories, items in store, and more. All of this is powered by a mysql database.
 */

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class Main extends Application {

    public void start(Stage primaryStage) {
//----------------------------------------VARIABLE INITIALIZATION-----------------------------------------------------------
        ArrayList<ImgRect> cartRectList = new ArrayList<>();
        final int SCENEHEIGHT = 600;
        final int SCENEWIDTH = 1000;

        final Font globalFont = Font.font("Times New Roman", FontWeight.SEMI_BOLD, 20);

        AtomicReference<User> userAccount = new AtomicReference<>();

        ComboBox<String> categoriesBoxCopyAddItem = new ComboBox<>();
        ComboBox<String> categoriesBoxCopy = new ComboBox<>();
        ComboBox<String> removeItemCategoriesBox = new ComboBox<>();
        ComboBox<String> categoriesBox = new ComboBox<>();

        //AtomicRefeneces allow us to use them in lambdas, as opposed to regular variables
        //Open a connection to the database
        AtomicReference<Connection> connection = new AtomicReference<>();
        try {
            connection.set(DriverManager.getConnection("jdbc:mysql://localhost:/finalproject", "projectAdmin", "password"));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        AtomicReference<String> query = new AtomicReference<>("");
        AtomicReference<PreparedStatement> ps = new AtomicReference<>();
        AtomicReference<ResultSet> rs = new AtomicReference<>();
        AtomicReference<Double> totalPrice = new AtomicReference<>(0.0);
        AtomicReference<Double> shipCost = new AtomicReference<>(0.0);

//----------------------------------------MENU-----------------------------------------------------------------------------
        BorderPane menuRootPane = new BorderPane();
        menuRootPane.setStyle("-fx-background-image:url('bg.png');");

        //Buttons to navigate the page
        Button cartButton = new Button("Cart");
        FlowPane.setMargin(cartButton, new Insets(50, 0, 0, 0));
        Button cancelButton = new Button("Logout");
        FlowPane.setMargin(cancelButton, new Insets(50, 0, 0, 0));
        Button goToCommentScene = new Button("Give feedback");
        FlowPane.setMargin(goToCommentScene, new Insets(50, 0, 0, 0));
        //Add buttons to pane
        FlowPane buttonPane = new FlowPane(25, 30, cartButton, goToCommentScene, cancelButton);
        //Center buttons
        buttonPane.setAlignment(Pos.CENTER);


        //Set up the ComboBox for the item categories
        Text selectCategory = new Text("Select a category");
        selectCategory.setFont(globalFont);

        Text menuEventText = new Text();
        menuEventText.setFont(globalFont);
        FlowPane categoriesPane = new FlowPane(Orientation.VERTICAL, 0, 20);
        menuEventText.relocate(415, 400);

        LinkedList<String> categories = new LinkedList<>(); //Holds the category names retrieved from database
        query.set("select cat_name from category"); //get all category names in category table
        try {
            ps.set(connection.get().prepareStatement(query.get()));
            rs.set(ps.get().executeQuery());
            while (rs.get().next()) //While there are categories
                categories.add(rs.get().getString("cat_name")); //Add them to list


        } catch (SQLException e) {
            e.printStackTrace();
        }


        //bind all the categories boxes items, so that when one changes, they all change
        categoriesBox.itemsProperty().bindBidirectional(categoriesBoxCopy.itemsProperty());
        categoriesBoxCopyAddItem.itemsProperty().bind(categoriesBox.itemsProperty());
        removeItemCategoriesBox.itemsProperty().bind(categoriesBox.itemsProperty());

        //Add categories, updates all boxes
        categoriesBox.setItems(FXCollections.observableArrayList(categories));


        categoriesPane.getChildren().addAll(selectCategory, categoriesBox);
        categoriesPane.setAlignment(Pos.CENTER);


        //Holds all the imgRects
        FlowPane rectPane = new FlowPane();
        rectPane.setAlignment(Pos.CENTER);
        //Never stretch rects
        rectPane.setRowValignment(VPos.BASELINE);


        //final list which contains the selected items
        LinkedList<ImgRect> imgRectList = new LinkedList<>();

        //When you select from the dropdown, render the ImgRects
        categoriesBox.setOnAction(event -> {
            //Clear the collections
            rectPane.getChildren().clear();
            imgRectList.clear();
            //Retrieve the products with matching category
            query.set("select * from product, category where cat_name = '" + categoriesBox.getValue() + "' and product.categoryid = category.category_id group by productname");
            try {
                ps.set(connection.get().prepareStatement(query.get()));
                rs.set(ps.get().executeQuery());
                while (rs.get().next()) {
                    //Add found products as ImgRect to container
                    imgRectList.add(new ImgRect(new Product(rs.get().getInt("productID"), rs.get().getInt("CategoryID"),
                            rs.get().getInt("quantity"), rs.get().getString("productname"),
                            rs.get().getString("productDescription"), rs.get().getDouble("price"), rs.get().getDouble("shipmentcost")), "medium"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //Render the ImgRects
            for (ImgRect x : imgRectList) {

                //Temporary pane for event handling
                Pane tempPane = x.clone().render();
                rectPane.getChildren().add(tempPane); //Adding clones prevents duplicate children added exception

                tempPane.setOnMousePressed(innerEvent -> {
                    menuEventText.setText(x.getName() + " added to cart.");
                    tempPane.setStyle("-fx-background-color: silver;");
                    cartRectList.add(x);
                });

                tempPane.setOnMouseReleased(innerEvent -> tempPane.setStyle("-fx-background-color: white;-fx-border-color: black;"));

                tempPane.setOnMouseEntered(innerEventt -> menuEventText.setText(x.getDesc()));

                tempPane.setOnMouseExited(innerEvent -> menuEventText.setText(""));
            }
        });

        //Draw to root
        menuRootPane.setBottom(rectPane);
        menuRootPane.setCenter(categoriesPane);
        menuRootPane.setTop(buttonPane);
        menuRootPane.getChildren().add(menuEventText);

        Scene menuScene = new Scene(menuRootPane, SCENEWIDTH, SCENEHEIGHT);
//------------------------------------------------END MENU-------------------------------------------------------------

//------------------------------------------------CART-----------------------------------------------------------------
        Pane cartRootPane = new Pane();

        Scene cartScene = new Scene(cartRootPane, SCENEWIDTH, SCENEHEIGHT);

        FlowPane centerGroupPane = new FlowPane(10, 0);
        centerGroupPane.setAlignment(Pos.CENTER);

        ScrollPane centerPane = new ScrollPane();
        centerPane.setPrefSize(SCENEWIDTH, 400);

        FlowPane cartPane = new FlowPane(Orientation.VERTICAL);

        FlowPane pricePane = new FlowPane(Orientation.VERTICAL, 0, 9);

        FlowPane removePane = new FlowPane(Orientation.VERTICAL);

        Text eventText = new Text();
        eventText.setFont(globalFont);
        eventText.relocate(300, 415);

        //Holds prices
        Text totalText = new Text("");
        totalText.setFont(globalFont);
        Text subtotalText = new Text("");
        Text shipText = new Text("");

        //Clear the cart
        Button cartClearButton = new Button("Clear cart");
        cartClearButton.setOnAction(event -> {
            eventText.setText("Cart cleared..");
            cartRectList.clear();
            cartPane.getChildren().clear();
            pricePane.getChildren().clear();
            removePane.getChildren().clear();

        });

        //Render cart
        cartButton.setOnAction(event -> {
            //Clear collections
            cartPane.getChildren().clear();
            pricePane.getChildren().clear();
            removePane.getChildren().clear();


            for (ImgRect x : cartRectList) {
                x.setSizeOfRect("small"); //Set rect sizes to small
                totalPrice.updateAndGet(v -> v + x.getPrice()); //equivalent to totalPrice += x.getPrice()
                shipCost.updateAndGet(v -> v + x.getShipCost()); //equivalent to shopCost += x.getShipCost)
                cartPane.getChildren().add(x.clone().render()); //Adding clones prevents duplicate children added exception
                pricePane.getChildren().add(new Text("$" + String.format("%,.2f", x.getPrice())));

                Button removeButton = new Button("Remove");
                //Remove an item from cart
                removeButton.setOnAction(innerEvent -> {
                    eventText.setText(x.getName() + " has been removed from your cart");

                    totalPrice.updateAndGet(v -> v - x.getPrice()); //equivalent to totalPrice -= x.getPrice()
                    shipCost.updateAndGet(v -> v - x.getShipCost());//equivalent to shopCost -= x.getShipCost)

                    //update Texts
                    totalText.setText("TOTAL: $" + String.format("%,.2f", totalPrice.get() + shipCost.get()));
                    shipText.setText("Shipping: $" + String.format("%,.2f", shipCost.get()));
                    subtotalText.setText("Subtotal: $" + String.format("%,.2f", totalPrice.get()));
                    //Remove from center panes
                    pricePane.getChildren().remove(cartRectList.indexOf(x));
                    cartPane.getChildren().remove(cartRectList.indexOf(x));
                    removePane.getChildren().remove(cartRectList.indexOf(x));
                    //Remove from list
                    cartRectList.remove(x);
                });
                removePane.getChildren().add(removeButton);
            }

            totalText.setText("Total: $" + String.format("%,.2f", totalPrice.get() + shipCost.get()));
            shipText.setText("Shipping: $" + String.format("%,.2f", shipCost.get()));
            subtotalText.setText("Subtotal: $" + String.format("%,.2f", totalPrice.get()));

            //Separate total with line
            Line totalLine = new Line(totalText.getLayoutX(), totalText.getLayoutY(), totalText.getLayoutX() + 100, totalText.getLayoutY());
            totalLine.setStrokeWidth(3);

            //Essentially infinite length, Scrollpane will adjust to match
            cartPane.setPrefWrapLength(cartRectList.size() * 25);
            removePane.setPrefWrapLength(cartRectList.size() * 25);
            pricePane.setPrefWrapLength((cartRectList.size() + 90) * 25);

            pricePane.getChildren().addAll(subtotalText, shipText, totalLine, totalText);

            //Set scene to the cart
            primaryStage.setScene(cartScene);
        });

        Button checkOut = new Button("Check out");
        Button backToMenu = new Button("Back to menu");

        //Move buttons to proper positions on pane
        checkOut.relocate(300, 450);
        cartClearButton.relocate(525, 450);
        backToMenu.relocate(400, 450);

        //Add flowpanes to center flowpane group
        centerGroupPane.getChildren().addAll(removePane, cartPane, pricePane);
        //Add center flowpane to scrollpane
        centerPane.setContent(centerGroupPane);
        centerPane.setFitToWidth(true);

        //Add items to root
        cartRootPane.getChildren().addAll(cartClearButton, checkOut, eventText, backToMenu, centerPane);


//----------------------------------------------------END CART------------------------------------------------------------

//---------------------------------------------------ADMIN LANDING-----------------------------------------------------------
        FlowPane adminRootPane = new FlowPane(Orientation.VERTICAL, 0, 10);
        //Align objects by their center
        adminRootPane.setColumnHalignment(HPos.CENTER);
        adminRootPane.setStyle("-fx-background-image:url('bg.png');");
        adminRootPane.setAlignment(Pos.CENTER);

        Text adminActionText = new Text("");
        adminActionText.setFont(globalFont);

        Text adminWelcome = new Text("Hello admin, please select an action below:");
        adminWelcome.setFont(globalFont);

        //Action buttons
        Button addCategory = new Button("Add Category");
        addCategory.setMaxWidth(150);
        Button removeCategory = new Button("Remove Category");
        removeCategory.setMaxWidth(150);
        Button addItem = new Button("Add Item");
        addItem.setMaxWidth(150);
        Button removeItem = new Button("Remove Item");
        removeItem.setMaxWidth(150);
        Button viewComments = new Button("View comments");
        viewComments.setMaxWidth(150);
        Button adminLogout = new Button("Logout");
        adminLogout.setMaxWidth(150);

        adminRootPane.getChildren().addAll(adminActionText, adminWelcome, addCategory, removeCategory, addItem, removeItem, viewComments, adminLogout);

        Scene adminLoginScene = new Scene(adminRootPane, SCENEWIDTH, SCENEHEIGHT);
//----------------------------------------------------ADMIN LANDING END---------------------------------------------------------

//----------------------------------------------------LOG IN-----------------------------------------------------------------
        BorderPane loginRootPane = new BorderPane();
        loginRootPane.setStyle("-fx-background-image:url('bg.png');");

        FlowPane loginCenterPane = new FlowPane(Orientation.VERTICAL, 0, 10);
        loginCenterPane.setAlignment(Pos.CENTER);
        Text welcomeText = new Text("Welcome to Healthy Living");
        welcomeText.setFont(globalFont);

        Text authors = new Text("By Naciur and John");
        Text loginText = new Text("Please log in below:");
        //Input username
        TextField accountField = new TextField();
        accountField.setPromptText("Account");
        //Input password
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        Button loginButton = new Button("Login");
        Button createAcc = new Button("Create account");
        Text incorrectLogin = new Text();
        incorrectLogin.setFill(Color.RED);

        loginButton.setOnAction(event -> {
            // Match username and password, CASE SENSITIVE, then return firstname and lastname
            query.set("select * from user_table where username = '" + accountField.getText() + "' COLLATE utf8mb4_bin" +
                    " and  password = '" + passField.getText() + "' COLLATE utf8mb4_bin");
            try {
                ps.set(connection.get().prepareStatement(query.get()));
                rs.set(ps.get().executeQuery());
                if (!rs.get().first()) //No matching login info
                    incorrectLogin.setText("Incorrect login, please try again");
                else {
                    //Set the userAccount object to the found user
                    userAccount.set(new User(rs.get().getInt("userid"), rs.get().getString("firstName"), rs.get().getString("lastname"),
                            rs.get().getString("Username"), rs.get().getString("password"), rs.get().getString("zipcode"),
                            rs.get().getString("city"), rs.get().getString("state"), rs.get().getString("Country")));

                    //Check if found account is an admin account by comparing names
                    query.set("select * from Administration where firstname = '" + rs.get().getString("firstname") + "' and lastname = '" + rs.get().getString("lastname") + "'");
                    ps.set(connection.get().prepareStatement(query.get()));
                    rs.set(ps.get().executeQuery());
                    if (!rs.get().first()) // Found account is not an admin
                        primaryStage.setScene(menuScene);
                    else //found account is admin
                        primaryStage.setScene(adminLoginScene);
                    //Reset input fields
                    accountField.setText("");
                    passField.setText("");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        loginCenterPane.getChildren().addAll(welcomeText, authors, loginText, accountField, passField, loginButton, createAcc, incorrectLogin);
        loginRootPane.setCenter(loginCenterPane);

        Scene landingScene = new Scene(loginRootPane, SCENEWIDTH, SCENEHEIGHT);
//----------------------------------------------------LOGIN END---------------------------------------------------------

//----------------------------------------------------ADMIN ACTION: ADD CATEGORY----------------------------------------------
        FlowPane addCategoryRootPane = new FlowPane(Orientation.VERTICAL, 0, 10);
        addCategoryRootPane.setStyle("-fx-background-image:url('bg.png');");
        addCategoryRootPane.setAlignment(Pos.CENTER);
        Text addCategoryText = new Text("Enter new category name");
        TextField newCategoryField = new TextField();
        Button addCategoryAction = new Button("Add category");
        Button addCategoryCancel = new Button("Cancel");
        addCategoryAction.setOnAction(event -> {

            //Insert the inputted category name and description ( description is unused)
            query.set("insert into category(cat_name, catDescription) values ('" + newCategoryField.getText() + "' , '')");
            try {
                ps.set(connection.get().prepareStatement(query.get()));
                if (ps.get().executeUpdate() == 1) { //Success
                    adminActionText.setText("Category added.");
                    categories.add(newCategoryField.getText());
                    categoriesBox.setItems(FXCollections.observableArrayList(categories));
                } else
                    adminActionText.setText("Failed to add category");
            } catch (SQLException e) {
                e.printStackTrace();
            }

            primaryStage.setScene(adminLoginScene);
        });

        addCategoryRootPane.getChildren().addAll(addCategoryText, newCategoryField, addCategoryAction, addCategoryCancel);

        Scene addCategoryScene = new Scene(addCategoryRootPane, SCENEWIDTH, SCENEHEIGHT);
//----------------------------------------------------END ADMIN ACTION: ADD CATEGORY------------------------------------

//----------------------------------------------------ADMIN ACTION: REMOVE CATEGORY---------------------------------------
        FlowPane removeCategoryRootPane = new FlowPane(Orientation.VERTICAL, 0, 10);
        removeCategoryRootPane.setStyle("-fx-background-image:url('bg.png');");
        removeCategoryRootPane.setAlignment(Pos.CENTER);
        Text selectCategoryAction = new Text("Select a category to remove");
        Button removeCategoryAction = new Button("Remove Category");
        Button removeCategoryCancel = new Button("Cancel");

        removeCategoryAction.setOnAction(event -> {
            //Delete category with matching name
            query.set("delete from category where cat_name = '" + categoriesBoxCopy.getValue() + "'");
            try {
                ps.set(connection.get().prepareStatement(query.get()));
                if (ps.get().executeUpdate() >= 1) { //Success
                    adminActionText.setText("Category removed");
                    categories.remove(categoriesBoxCopy.getValue());
                    categoriesBoxCopy.setItems(FXCollections.observableArrayList(categories));
                } else
                    adminActionText.setText("Failed to remove category");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            primaryStage.setScene(adminLoginScene);
        });

        removeCategoryRootPane.getChildren().addAll(selectCategoryAction, categoriesBoxCopy, removeCategoryAction, removeCategoryCancel);

        Scene removeCategoryScene = new Scene(removeCategoryRootPane, SCENEWIDTH, SCENEHEIGHT);
//--------------------------------------------------ADMIN ACTION: REMOVE CATEGORY END-----------------------------------------

//--------------------------------------------------ADMIN ACTION: ADD ITEM----------------------------------------------------
        FlowPane addItemRootPane = new FlowPane(Orientation.VERTICAL, 0, 10);
        addItemRootPane.setStyle("-fx-background-image:url('bg.png');");
        addItemRootPane.setAlignment(Pos.CENTER);

        //TextFields to enter Product info
        Text selectCategoryAddItem = new Text("Select new items category");
        Text enterItemInfo = new Text("Enter new item's information:");
        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        TextField descField = new TextField();
        descField.setPromptText("Product Description");
        TextField priceField = new TextField();
        priceField.setPromptText("Product Price");
        TextField stockField = new TextField();
        stockField.setPromptText("Product Stock");
        TextField shipPriceField = new TextField();
        shipPriceField.setPromptText("Shipping price");

        Button addItemAction = new Button("Add Item");
        Button addItemCancel = new Button("Cancel");

        addItemRootPane.getChildren().addAll(selectCategoryAddItem, categoriesBoxCopyAddItem, enterItemInfo,
                nameField, descField, priceField, shipPriceField, stockField, addItemAction, addItemCancel);

        addItemAction.setOnAction(event -> {

            //get the category ID of the selected category
            query.set("select category_id from category where cat_name = '" + categoriesBoxCopyAddItem.getValue() + "'");
            int catID = 1;
            try {
                ps.set(connection.get().prepareStatement(query.get()));
                rs.set(ps.get().executeQuery());
                if (rs.get().first())
                    catID = rs.get().getInt("category_id");
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //get the maximum productID, increment by 1 to get the new max
            query.set("select max(productid) from product");
            try {
                ps.set(connection.get().prepareStatement(query.get()));
                rs.set(ps.get().executeQuery());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                //Insert the product into the database using Product's getInsertQuery() method
                query.set(new Product(rs.get().getInt("productid") + 1, catID, Integer.parseInt(stockField.getText()), nameField.getText(), descField.getText(),
                        Double.parseDouble(priceField.getText()), Double.parseDouble(shipPriceField.getText())).getInsertQuery());

                ps.set(connection.get().prepareStatement(query.get()));
                if (ps.get().executeUpdate() == 1)
                    adminActionText.setText("Item succesfully added.");
                else
                    adminActionText.setText("Failed to add item.");

            } catch (SQLException e) {
                e.printStackTrace();
            }

            primaryStage.setScene(adminLoginScene);
        });


        Scene addItemScene = new Scene(addItemRootPane, SCENEWIDTH, SCENEHEIGHT);
//--------------------------------------------------END ADMIN ACTION: ADD ITEM----------------------------------------------------


//--------------------------------------------------ADMIN ACTION: REMOVE ITEM----------------------------------------------------
        BorderPane removeItemRootPane = new BorderPane();
        removeItemRootPane.setStyle("-fx-background-image:url('bg.png');");

        //The Following are used in the confirmation scene:
        Button confirmRemoveItem = new Button("Confirm");
        Button confirmRemoveCancel = new Button("Cancel");
        Text confirmRemoveText = new Text("Are you sure you want to remove this item?");
        confirmRemoveText.setFont(globalFont);
        FlowPane removeItemConfirmPane = new FlowPane(Orientation.VERTICAL, 0, 10);
        removeItemConfirmPane.setAlignment(Pos.CENTER);
        removeItemConfirmPane.setColumnHalignment(HPos.CENTER);
        Scene removeItemConfirmScene = new Scene(removeItemConfirmPane, SCENEWIDTH, SCENEHEIGHT);


        Button removeItemCancelButton = new Button("Return");

        //Add button to pane
        FlowPane removeItemButtonPane = new FlowPane(25, 30, removeItemCancelButton);
        FlowPane.setMargin(removeItemCancelButton, new Insets(100, 0, 0, 0));
        //Center button
        removeItemButtonPane.setAlignment(Pos.CENTER);


        //Set up the ComboBox for the item categories
        Text removeItemSelectCategory = new Text("Select a category");
        removeItemSelectCategory.setFont(globalFont);

        Text removeItemEventText = new Text();
        removeItemEventText.setFont(globalFont);
        removeItemEventText.setText("Select an item to remove.");
        FlowPane removeItemCategoriesPane = new FlowPane(Orientation.VERTICAL, 0, 20);

        removeItemCategoriesPane.getChildren().addAll(removeItemSelectCategory, removeItemCategoriesBox, removeItemEventText);
        removeItemCategoriesPane.setAlignment(Pos.CENTER);


        //Holds all the imgRects
        FlowPane removeItemsRectPane = new FlowPane();
        removeItemsRectPane.setAlignment(Pos.CENTER);
        //Never stretch rects
        removeItemsRectPane.setRowValignment(VPos.BASELINE);


        //When you select from the dropdown, render the ImgRects
        removeItemCategoriesBox.setOnAction(event -> {
            //Clear the collections
            removeItemsRectPane.getChildren().clear();
            imgRectList.clear();
            //Retrieve the products with matching category
            query.set("select * from product, category where cat_name = '" + removeItemCategoriesBox.getValue() + "' and product.categoryid = category.category_id group by productname");
            try {
                ps.set(connection.get().prepareStatement(query.get()));
                rs.set(ps.get().executeQuery());
                while (rs.get().next()) {
                    imgRectList.add(new ImgRect(new Product(rs.get().getInt("productID"), rs.get().getInt("CategoryID"),
                            rs.get().getInt("quantity"), rs.get().getString("productname"),
                            rs.get().getString("productDescription"), rs.get().getDouble("price"), rs.get().getDouble("shipmentcost")), "medium"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //Render the ImgRects
            for (ImgRect x : imgRectList) {

                //Temporary pane for event handling
                Pane tempPane = x.clone().render();//Adding clones prevents duplicate children added exception
                removeItemsRectPane.getChildren().add(tempPane);

                tempPane.setOnMousePressed(innerEvent -> {
                    removeItemConfirmPane.getChildren().clear(); //Clear the pane so the new one can be placed
                    removeItemConfirmPane.getChildren().addAll(confirmRemoveText, x.clone().render(), confirmRemoveItem, confirmRemoveCancel);
                    primaryStage.setScene(removeItemConfirmScene);
                    query.set(x.getProduct().getRemoveQuery());
                });

                confirmRemoveItem.setOnAction(innerEvent -> {
                    //Remove selected product using Product's getRemoveQuery() method
                    //query.set(x.getProduct().getRemoveQuery());
                    System.out.println(query.get());
                    try {
                        ps.set(connection.get().prepareStatement(query.get()));
                        if (ps.get().executeUpdate() >= 1) //Success
                        {
                            removeItemCategoriesBox.setValue("");
                            adminActionText.setText("Item removed");
                        }
                        else
                            adminActionText.setText("Failed to remove item");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    primaryStage.setScene(adminLoginScene);
                });
            }
        });

        //Draw to root
        removeItemRootPane.setBottom(removeItemsRectPane);
        removeItemRootPane.setCenter(removeItemCategoriesPane);
        removeItemRootPane.setTop(removeItemButtonPane);

        Scene removeItemScene = new Scene(removeItemRootPane, SCENEWIDTH, SCENEHEIGHT);
//--------------------------------------------------END ADMIN ACTION: REMOVE ITEM----------------------------------------------------

//--------------------------------------------------ADMIN ACTION: VIEW COMMENTS------------------------------------------------------
        Pane viewCommentPane = new Pane();

        Scene viewCommentScene = new Scene(viewCommentPane, SCENEWIDTH, SCENEHEIGHT);

        ScrollPane commentScrollPane = new ScrollPane();
        commentScrollPane.setPrefSize(SCENEWIDTH, SCENEHEIGHT);
        commentScrollPane.setFitToWidth(true);
        commentScrollPane.relocate(0, 50);

        GridPane commentCenterPane = new GridPane();
        commentCenterPane.setStyle("-fx-border-color:black");
        commentCenterPane.setVgap(10);
        commentCenterPane.setHgap(100);

        Text nameTopText = new Text("Name:");
        nameTopText.setFont(globalFont);
        nameTopText.relocate(commentScrollPane.getLayoutX(), 10);
        Text ratingTopText = new Text("Rating:");
        ratingTopText.setFont(globalFont);
        ratingTopText.relocate(commentScrollPane.getLayoutX() + 245, 10);
        Text commentTopText = new Text("Comment:");
        commentTopText.setFont(globalFont);
        commentTopText.relocate(commentScrollPane.getLayoutX() + 400, 10);
        Button commentGoBack = new Button("Return");
        commentGoBack.setOnAction(event -> primaryStage.setScene(adminLoginScene));
        commentGoBack.relocate(700, 10);

        //Get firstname, lastname, rating, and comment from users who left a comment
        viewComments.setOnAction(event -> {
            query.set("select user_table.firstname, user_table.lastname, comment_table.rating, comment_table.comment from user_table inner join comment_table on comment_table.userid = user_table.userid");
            try {
                ps.set(connection.get().prepareStatement(query.get()));
                rs.set(ps.get().executeQuery());
                int rowCount = 1; //To go to next row with every loop

                while (rs.get().next()) { //While rs has items
                    Text nameText = new Text(rs.get().getString("firstname") + " " + rs.get().getString("lastname"));
                    nameText.setFont(globalFont);
                    Text ratingText = new Text(Integer.toString(rs.get().getInt("rating")));
                    ratingText.setFont(globalFont);

                    //Set rating color based on score
                    if (Integer.parseInt(ratingText.getText()) < 4)
                        ratingText.setFill(Color.RED);
                    else if (Integer.parseInt(ratingText.getText()) < 7)
                        ratingText.setFill(Color.ORANGE);
                    else
                        ratingText.setFill(Color.GREEN);

                    TextArea commentText = new TextArea(rs.get().getString("comment"));
                    commentText.setEditable(false); //Cant be edited
                    commentText.setWrapText(true); //Wraps if text is longer than width

                    commentCenterPane.add(nameText, 0, rowCount);
                    commentCenterPane.add(ratingText, 1, rowCount);
                    commentCenterPane.add(commentText, 2, rowCount);
                    rowCount++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            primaryStage.setScene(viewCommentScene);

        });
        commentScrollPane.setContent(commentCenterPane);

        viewCommentPane.getChildren().addAll(nameTopText, ratingTopText, commentTopText, commentGoBack, commentScrollPane);
//--------------------------------------------------END ADMIN ACTION: VIEW COMMENTS--------------------------------------------------------

//--------------------------------------------------ADD COMMENT-------------------------------------------------------------------
        FlowPane addCommentPane = new FlowPane(Orientation.VERTICAL, 0, 10);
        addCommentPane.setAlignment(Pos.CENTER);
        addCommentPane.setStyle("-fx-background-image:url('bg.png');");

        Text addCommentText = new Text("Select a rating, then type your comment.");
        addCommentText.setFont(globalFont);

        //Holds the rating numbers
        ComboBox<Integer> ratingsBox = new ComboBox<>();
        ratingsBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        ratingsBox.setPromptText("Rating");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Type your comment here");
        commentArea.setWrapText(true);
        Button addCommentAction = new Button("Submit comment");
        Button addCommentCancel = new Button("Cancel");

        addCommentAction.setOnAction(event -> {
            if (commentArea.getLength() > 3000) //Comment length is 3000 characters
                addCommentText.setText("Comment length exceeds character limit of 3000, please remove " + (commentArea.getLength() - 3000) + " characters.");
            else {//Good comment length
                //Insert comment into database
                query.set("insert into comment_table(userid, rating, comment) values (" + userAccount.get().getUserID() + ", " + ratingsBox.getValue() +
                        ", '" + commentArea.getText() + "')");
                try {
                    ps.set(connection.get().prepareStatement(query.get()));
                    if (ps.get().executeUpdate() >= 1) //Success
                        menuEventText.setText("Comment successfully added");
                    else
                        menuEventText.setText("Failed to add comment");

                    primaryStage.setScene(menuScene);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        addCommentPane.getChildren().addAll(addCommentText, ratingsBox, commentArea, addCommentAction, addCommentCancel);

        Scene addCommentScene = new Scene(addCommentPane, SCENEWIDTH, SCENEHEIGHT);
//--------------------------------------------------END ADD COMMENT---------------------------------------------------------------

//--------------------------------------------------CHECK OUT------------------------------------------------------------------------
        FlowPane checkOutRootPane = new FlowPane(Orientation.VERTICAL, 0, 10);
        Scene checkOutScene = new Scene(checkOutRootPane, SCENEWIDTH, SCENEHEIGHT);

        checkOutRootPane.setStyle("-fx-background-image:url('bg.png');");
        checkOutRootPane.setAlignment(Pos.CENTER);


        Text checkOutTopText = new Text("Order placed with the following account:");
        checkOutTopText.setFont(globalFont);
        Text checkOutAccount = new Text("");
        Text checkOutAddress = new Text("");
        Text checkOutCard = new Text("");
        Button checkOutOk = new Button("Ok");
        checkOutRootPane.getChildren().addAll(checkOutTopText, checkOutAccount, checkOutAddress, checkOutCard, checkOutOk);

        checkOut.setOnAction(event -> {
            if (cartPane.getChildren().size() == 0) //No items in cart
                eventText.setText("Cannot place an order with no items!");
            else {
                checkOutAccount.setText("Name: " + userAccount.get().getFullname());
                checkOutAddress.setText("Address: " + userAccount.get().getFullAdress());
                //add to order_table
                query.set("insert into order_table(userid, orderprice) values (" + userAccount.get().getUserID() + ", " + totalPrice.get() + ")");

                try {
                    ps.set(connection.get().prepareStatement(query.get()));
                    if (ps.get().executeUpdate() >= 1) { //success
                        query.set("select max(orderid) from order_table");
                        ps.set(connection.get().prepareStatement(query.get()));
                        rs.set(ps.get().executeQuery());
                        if (rs.get().first()) {
                            int maxOrderId = rs.get().getInt("max(orderid)"); //Get the orderid for this order
                            ObjectOutputStream orderOutput = new ObjectOutputStream(new FileOutputStream("orders/order" + maxOrderId + ".ser"));
                            for (ImgRect x : cartRectList) { //Write order to file
                                orderOutput.writeObject(x.getProduct());
                            }

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //Format according to SQL datetime convention
                            Date currentDate = new Date(); //Current date
                            GregorianCalendar advancedDate = new GregorianCalendar(); //Arrival date
                            advancedDate.add(Calendar.DAY_OF_WEEK, ((int) (Math.random() * 5)) + 2); //increase the shipping date by between 2 to 7 days
                            advancedDate.set(Calendar.HOUR_OF_DAY, (int) (Math.random() * 24)); //Set the hour to a random hour
                            advancedDate.set(Calendar.MINUTE, (int) (Math.random() * 60)); //Same for minute
                            advancedDate.set(Calendar.SECOND, (int) (Math.random() * 60)); //Same for second

                            //insert into shipment table
                            query.set("insert into shipment(orderid, shipmentdate, receivingdate) values " +
                                    "( " + maxOrderId + ", '" +
                                    dateFormat.format(currentDate) + "', '" +
                                    dateFormat.format(advancedDate.getTime()) + "')");
                            ps.set(connection.get().prepareStatement(query.get()));
                            if (ps.get().executeUpdate() >= 1) { //success
                                checkOutTopText.setText("Order placed, expected arrival: " + new SimpleDateFormat("MM/dd/yyyy HH:mm").format(advancedDate.getTime()));
                            } else { //fail
                                checkOutTopText.setText("Failed to place order");
                                //On order fail, delete contents of placed order
                                new File("orders/order" + maxOrderId + ".ser").delete();
                                query.set("delete from order * where orderid = " + maxOrderId);
                                ps.set(connection.get().prepareStatement(query.get()));
                                ps.get().executeUpdate();
                            }
                        }
                    } else
                        checkOutTopText.setText("Failed to place order");
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            }
            primaryStage.setScene(checkOutScene);
        });

// --------------------------------------------------CREATE ACCOUNT----------------------------------------------------
        GridPane createAccBasePane = new GridPane();
        createAccBasePane.setStyle("-fx-background-image:url('bg.png');");
        createAccBasePane.setAlignment(Pos.CENTER);
        createAccBasePane.setHgap(10);
        createAccBasePane.setVgap(10);

        Text newAccText = new Text("Enter new account information:");
        createAccBasePane.add(newAccText, 0, 0);

        //Username and password fields
        TextField newAccField = new TextField();
        newAccField.setPromptText("Username");
        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("Password");
        createAccBasePane.add(newAccField, 0, 1);
        createAccBasePane.add(newPassField, 1, 1);

        //Name fields
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        createAccBasePane.add(firstNameField, 0, 2);
        createAccBasePane.add(lastNameField, 1, 2);

        //Zipcode and city fields
        TextField zipCodeField = new TextField();
        zipCodeField.setPromptText("Zip Code");
        TextField cityField = new TextField();
        cityField.setPromptText("City");
        createAccBasePane.add(zipCodeField, 0, 3);
        createAccBasePane.add(cityField, 1, 3);

        //State and country field
        TextField stateField = new TextField();
        stateField.setPromptText("State (or blank if N/A)");
        TextField countryField = new TextField();
        countryField.setPromptText("Country");
        createAccBasePane.add(stateField, 0, 4);
        createAccBasePane.add(countryField, 1, 4);

        Button createAccAction = new Button("Create Account");

        createAccAction.setOnAction(event -> {

            //Check if username already exists
            query.set("select username from user_table where username = '" + newAccField.getText() + "'");
            newAccText.setFill(Color.RED);
            try {
                ps.set(connection.get().prepareStatement(query.get()));
                rs.set(ps.get().executeQuery());
                if (rs.get().first()) //Account with given username already exists
                {
                    newAccText.setText("Username already exists, please enter a new username");
                } else {
                    //Only state can be empty, so make sure everything else is filled in
                    if (nameField.getText().isEmpty() || lastNameField.getText().isEmpty() || newAccField.getText().isEmpty() || newPassField.getText().isEmpty() ||
                            zipCodeField.getText().isEmpty() || cityField.getText().isEmpty() || countryField.getText().isEmpty())
                        newAccText.setText("Please fill in the required information.");
                    else {
                        newAccText.setFill(Color.BLACK);
                        newAccText.setText("Enter new account information:");
                        query.set("select max(userid) from user_table"); //Get max userID then add one to make a new user
                        ps.set(connection.get().prepareStatement(query.get()));
                        rs.set(ps.get().executeQuery());

                        if (rs.get().first()) //Add user with entered info using User's getInsertQuery() method
                            query.set(new User(rs.get().getInt("max(userid)") + 1, nameField.getText(), lastNameField.getText(), newAccField.getText(), newPassField.getText(),
                                    zipCodeField.getText(), cityField.getText(), stateField.getText(), countryField.getText()).getInsertQuery());

                        ps.set(connection.get().prepareStatement(query.get()));
                        if (ps.get().executeUpdate() == 1)//success
                            incorrectLogin.setText("Account successfully added, welcome " + nameField.getText());
                        else
                            incorrectLogin.setText("Failed to add account");
                        primaryStage.setScene(landingScene);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });
        Button createAccCancel = new Button("Cancel");
        createAccBasePane.add(createAccAction, 0, 5);
        createAccBasePane.add(createAccCancel, 1, 5);

        Scene createAccScene = new Scene(createAccBasePane, SCENEWIDTH, SCENEHEIGHT);
//----------------------------------------------------SCENE SWITCH EVENT HANDLING-----------------------------------------
        //When user presses enter in either login field:
        passField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                loginButton.fire();
        });

        accountField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                loginButton.fire();
        });

        backToMenu.setOnAction(event -> primaryStage.setScene(menuScene));

        addCategory.setOnAction(event -> primaryStage.setScene(addCategoryScene));

        removeCategory.setOnAction(event -> primaryStage.setScene(removeCategoryScene));

        addItem.setOnAction(event -> primaryStage.setScene(addItemScene));

        cancelButton.setOnAction(event -> primaryStage.setScene(landingScene));

        createAcc.setOnAction(event -> primaryStage.setScene(createAccScene));

        checkOutOk.setOnAction(event -> primaryStage.setScene(landingScene));

        addItemCancel.setOnAction(event -> primaryStage.setScene(adminLoginScene));

        createAccCancel.setOnAction(event -> primaryStage.setScene(landingScene));

        goToCommentScene.setOnAction(event -> primaryStage.setScene(addCommentScene));

        addCommentCancel.setOnAction(event -> primaryStage.setScene(menuScene));

        adminLogout.setOnAction(event -> primaryStage.setScene(landingScene));

        removeItem.setOnAction(event -> primaryStage.setScene(removeItemScene));

        removeItemCancelButton.setOnAction(event -> primaryStage.setScene(adminLoginScene));

        confirmRemoveCancel.setOnAction(event -> primaryStage.setScene(removeItemScene));

        addCategoryCancel.setOnAction(event -> primaryStage.setScene(adminLoginScene));

        removeCategoryCancel.setOnAction(event -> primaryStage.setScene(adminLoginScene));
//---------------------------------------------------SCENE SWITCH HANDLING END--------------------------------------------

        primaryStage.setTitle("Healthy Living");
        primaryStage.getIcons().add(new Image("file:resources/icon.png"));
        primaryStage.setScene(landingScene);
        primaryStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}
