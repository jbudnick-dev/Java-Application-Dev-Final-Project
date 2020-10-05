import java.io.Serializable;

public class Product implements Serializable {

	/**
	 * serialVersionUID = long for Serialization
	 * productID, categodyID, productStock, productName, productDescription, price, shipmentCost = Product information, stored in database
	 */
	private static final long serialVersionUID = 3415027079657536443L;
	private int productID, categoryID;
	private int productStock;
	private String productName;
	private String productDescription;
	private double price, shipmentCost;

	/**
	 * Product constructor, all fields should be retrieved from databse
	 * @param productID Product ID number
	 * @param categoryID ID of category Product belongs to
	 * @param productStock Product stock count
	 * @param productName Product name
	 * @param productDescription Prodcut description
	 * @param price Product price
	 * @param shipmentCost Cost to ship Product
	 */
	public Product(int productID, int categoryID, int productStock, String productName, String productDescription, double price, double shipmentCost)
	{
		this.productID = productID;
		this.categoryID = categoryID;
		this.productStock = productStock;
		this.productName = productName;
		this.productDescription = productDescription;
		this.price = price;
		this.shipmentCost = shipmentCost;
	}

	/**
	 * productID getter method
	 * @return Product's ID
	 */
	public int getProductID() {
		return productID;
	}

	/**
	 * Product stock getter method
	 * @return Product's stock
	 */
	public int getProductStock() {
		return productStock;
	}

	/**
	 * Product Stock setter method
	 * @param quantity Product's new stock count
	 */
	public void setProductStock( int quantity) {
		productStock = quantity;
	}

	/**
	 * Product name getter method
	 * @return Product's name
	 */
	public String getName() {
		return productName;
	}

	/**
	 * Product name setter method
	 * @param name Product's new name
	 */
	public void setName( String name) {
		productName = name;
	}

	/**
	 * Product description getter method
	 * @return Product's description
	 */
	public String getDescription() {
		return productDescription;
	}

	/**
	 * Product description setter method
	 * @param description Product's new description
	 */
	public void setDescription( String description) {
		productDescription  = description;
	}

	/**
	 * Product price getter method
	 * @return Product's price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Product price setter method
	 * @param price Product's new price
	 */
	public void setPrice(int price) {
		this.price  = price;
	}

	/**
	 * Product categoryID getter method
	 * @return Product's categoryID
	 */
	public int getCategoryID(){
		return categoryID;
	}

	/**
	 * Product shipmentCost getter method
	 * @return Product's shipmentCost
	 */
	public double getShipmentCost(){return shipmentCost;}

	/**
	 * Gets an sql query to insert this Product into a database
	 * @return An SQL insert query
	 */
	public String getInsertQuery()
	{
		return ("insert into Product (CategoryID, ClassName, ProductName, ProductDescription, Price, quantity, ShipmentCost) values" +
				" ("+ categoryID + ", '" + getClass() + "', '" + productName + "', '" + getDescription() +"', " + price + ", " + productStock + ", " + shipmentCost + ")" );
	}

	/**
	 * Gets an sql query to remove this Product into a database
	 * @return An SQL delete query
	 */
	public String getRemoveQuery()
	{
		return ("delete from product where productid = " + productID);
	}
}
