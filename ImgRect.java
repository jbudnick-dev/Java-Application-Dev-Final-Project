import javafx.geometry.Orientation;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class ImgRect extends Rectangle implements Cloneable {
    /**
     * objImage = The Image of the given rectangle
     * objname, objDesc = Text objects to represent the given objects name and description
     * smallObjDesc = Text object that holds the price when rect is size "small"
     * basePane =  The pane that the Object is written to
     * sizeOfRect = Stores the rects size
     * heldProduct = the Product the rect represents
     */
    private ImageView objImage;
    private Text objName, objDesc, smallObjDesc;
    private Pane basePane = new Pane();
    private String sizeOfRect;
    private Product heldProduct;

	/* DEPRECIATED - USE OTHER CONSTRUCTOR
	 * NON-Object constructor
	 * @param nameIn Name of new rect
	 * @param descIn Description of new rect
	 * @param price	Price of new rect
	 * @param sizeOfRect Size of new rect

	public ImgRect(String nameIn, String descIn, double price, String sizeOfRect) {
		super(150,50); //Changing this changes the size of the rectangles

		this.sizeOfRect = sizeOfRect;
		this.price = price;
		objName = new Text(nameIn);
		if(!descIn.endsWith("."))
			objDesc = new Text(descIn + ".");
		else
			objDesc = new Text(descIn);
		smallObjDesc = new Text(60,60, String.format("$" + String.format("%,.2f", price) ));
		objImage = new ImageView(getImgPath());
		//objImage.setImage(new Image("file:" + "Resources.img/food/" + nameIn.toLowerCase() + "_" + sizeOfRect + ".png"));
	//	objImage.setViewport(new Rectangle2D(0,0,this.getWidth(), this.getHeight()));
	}
	*/

    /**
     * ACTUAL CONSTRUCTOR - SHOULD BE USED IN ACTUTAL CODE
     *
     * @param prodIn The object the rectangle is representing
     * @param sizeOfRect The size of the rectangle
     */

    public ImgRect(Product prodIn, String sizeOfRect) {
        super(150, 50);  //Changing this changes the size of the rectangle
        heldProduct = prodIn;
        this.sizeOfRect = sizeOfRect;
        objName = new Text(prodIn.getName());
        if (!prodIn.getDescription().endsWith(".")) //Make sure the description ends with a .
            objDesc = new Text(prodIn.getDescription() + ".");
        else
            objDesc = new Text(prodIn.getDescription());

        smallObjDesc = new Text("$" + String.format("%,.2f", prodIn.getPrice()) + "\n$" + String.format("%,.2f", prodIn.getShipmentCost()) + " S+H");

        objImage = new ImageView(getImgPath());
        objImage.setPreserveRatio(true);

        //	objImage.setPreserveRatio(true);
        //	objImage = new ImageView(("file:Resources.img/product/" + prodIn.getName() + "_" + sizeOfRect + ".png"));

        //objImage.setViewport(new Rectangle2D(0,0,this.getWidth(), this.getHeight()));
    }


    /**
     * Draws the rectangle
     * @return The Pane the object is written to
     */
    public Pane render() {

        //	objImage.setImage(new Image("file:Resources.img/food/" + getName() + "_" + sizeOfRect +".png")); //Cloned objects need their images set
        if (sizeOfRect.equals("small"))
            this.setHeight(25);
        //Set image to fit pane
        objImage.setFitHeight(this.getHeight());
        objImage.setFitWidth(objImage.getFitHeight());

        //Set basePane to a fixed size
        basePane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        basePane.setPrefSize(this.getWidth(), this.getHeight());
        basePane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        //Add border
        basePane.setStyle("-fx-border-color: black; -fx-background-color:white;");

        //pane to hold the Text objects
        FlowPane pane = new FlowPane(Orientation.VERTICAL);
        if (sizeOfRect.equals("small"))
            pane.getChildren().addAll(objName);
        else
            pane.getChildren().addAll(objName, smallObjDesc);

        //Move pane to bit over halfway past the rectangle
        pane.relocate(basePane.getLayoutX() + 60, basePane.getLayoutY());
        basePane.getChildren().addAll(objImage, pane);

        return basePane;
    }

    /**
     * Name getter method
     *
     * @return Name of Object
     */
    public String getName() {
        return objName.getText();
    }

    /**
     * Description getter method
     *
     * @return Descrption of method
     */
    public String getDesc() {
        return objDesc.getText();
    }

    /**
     * Pane getter method
     *
     * @return Pane the rectangle is drawn to
     */
    public Pane getBasePane() {
        return basePane;
    }

    /**
     * Price getter method
     *
     * @return Food price
     */
    public double getPrice() {
        return heldProduct.getPrice();
    }

    /**
     * Shipment cost getter method
     *
     * @return Shipment cost
     */
    public double getShipCost() {
        return heldProduct.getShipmentCost();
    }

    /**
     * Image path getter method
     *
     * @return Path to rectangle's image
     */
    public String getImgPath() {
        return "file:Resources/img/" + getName() + ".png";
    }

    /**
     * Rectangle size setter method
     *
     * @param sizeIn New size for rectangle
     */
    public void setSizeOfRect(String sizeIn) {
        sizeOfRect = sizeIn;
    }

    public Product getProduct(){
        return heldProduct;
    }
    /**
     * Cloneable's clone method override
     * Needed to render clones of objects, or else exception will be thrown by Parent nodes
     *
     * @return Clone of this object with new references
     */
    @Override
    public ImgRect clone() {
        return new ImgRect(heldProduct , sizeOfRect);
    }

}
