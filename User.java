public class User {

	/**
	 * All fields are User information, stored in database
	 */
	private int userID;
	private String password;
	private String name;
	private String lastName;
	private String userName;
	private String zipCode;
	private String city;
	private String state;
	private String country;

	/**
	 * User constructor, fields should be retrieved from database
	 * @param userID User ID number
	 * @param Name User name
	 * @param Last User lastname
	 * @param UserName User username
	 * @param Password User password
	 * @param zipCode User zipcode
	 * @param city User city
	 * @param state User state
	 * @param country User country
	 */
	public User(int userID,String Name, String Last, String UserName, String Password, String zipCode, String city, String state, String country) {
		this.userID = userID;
		this.name = Name;
		this.lastName = Last;
		this.userName = UserName;
		this.password = Password;
		this.zipCode = zipCode;
		this.city = city;
		this.state = state;
		this.country = country;
	}

	/**
	 * userID getter method
	 * @return User's userID
	 */
	public int getUserID() {
		return userID;
	}

	/**
	 * A SQL query to insert this user into the database
	 * @return An insert SQL query
	 */
	public String getInsertQuery()
	{
		return ("insert into user_table (password, storecredit, pendingrequest, firstname, lastname, username, zipcode, "+
				"city, state, country) values ('" + password + "', 0.0, false, '" + name + "', '"+ lastName +
				"', '" + userName + "', '" + zipCode + "', '" + city + "', '" + state + "', '" + country + "')");
	}

	/**
	 * Convenience method returning the user's full name
	 * @return Users first and last name, separated by a space
	 */
	public String getFullname()
	{
		return (name + " " + lastName);
	}

	/**
	 * Conenience method returning the user's address
	 * @return The user's city, state(if applicable), country, and zipcode
	 */
	public String getFullAdress()
	{
		if(state.isEmpty()) //No state
		return (city + ", " + country + " "+ zipCode);
		else
		return (city + " " + state +", " + country + " "+ zipCode);
	}
}


