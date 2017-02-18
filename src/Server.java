import static spark.Spark.*;

import java.sql.ResultSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Server {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Server Started............ ");
		options("/*",
				(request, response) -> {

					String accessControlRequestHeaders = request
							.headers("Access-Control-Request-Headers");
					if (accessControlRequestHeaders != null) {
						response.header("Access-Control-Allow-Headers",
								accessControlRequestHeaders);
					}

					String accessControlRequestMethod = request
							.headers("Access-Control-Request-Method");
					if (accessControlRequestMethod != null) {
						response.header("Access-Control-Allow-Methods",
								accessControlRequestMethod);
					}

					return "OK";
				});

		before((request, response) -> response.header(
				"Access-Control-Allow-Origin", "*"));

		// code starts from here....

		get("/hello", (req, res) -> "Hello World");

		post("/login",
				(request, response) -> {
					System.out.println(request.body() + "---");
					String body = request.body();

					JSONObject responseData = new JSONObject();

					System.out.println("received data as " + body);
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);
						System.out.println("Data is parsed sucess ");

						if (jsonData.get("username") == null
								|| jsonData.get("password") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send username and password");
						} else {
							String userName = (String) jsonData.get("username");
							String password = (String) jsonData.get("password");
							JSONObject payload = new JSONObject();

							Dbcon db = new Dbcon();
							String sql = "select * from tbl_student where email='"
									+ userName
									+ "' and password='"
									+ password
									+ "'";
							ResultSet rs = db.select(sql);
							if (rs.next()) {
								responseData.put("result", true);
								responseData.put("description",
										"Login was sucess");
								payload.put("name", rs.getString("name"));
								payload.put("userId", rs.getString("id"));
								payload.put("branch", rs.getString("branch"));
								payload.put("address", rs.getString("address"));
								payload.put("phone", rs.getString("phone_no"));
								payload.put("admissionDateMilli",
										rs.getString("admdate_milli"));
								payload.put("college", rs.getString("college"));
								responseData.put("payload", payload);
							} else {
								responseData.put("result", false);
								responseData.put("description",
										"Login failed, Incorrect credentials");
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/getNotifications", (request, response) -> {
			System.out.println("getNotifications  API call " + request.body()
					+ " --- end ");
			String body = request.body();

			JSONObject responseData = new JSONObject();
			JSONParser jsonParser = new JSONParser();

			try {
				JSONObject jsonData = (JSONObject) jsonParser.parse(body);

				if (jsonData.get("userId") == null) {
					responseData.put("result", false);
					responseData.put("description", "Please send user ID");
				} else {
					JSONObject payload = new JSONObject();
					JSONArray dataarray = new JSONArray();
					Dbcon db = new Dbcon();

					String sql = "select * from tbl_notifications";
					ResultSet rs = db.select(sql);
					while (rs.next()) {
						JSONObject notify = new JSONObject();
						notify.put("title", rs.getString("title"));
						notify.put("description", rs.getString("description")
								.replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
						dataarray.add(notify);
					}
					responseData.put("result", true);
					responseData.put("description", "Sucessfully fetched ");
					responseData.put("payload", dataarray);
				}
			} catch (ParseException pe) {
				System.out.println("Error in parseing json data");
				System.out.println(pe);
				responseData.put("result", false);
				responseData.put("description", "Please send a valid json");
			}

			return responseData;
		});

		post("/editUserDetails",
				(request, response) -> {
					System.out.println("editUserDetails  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();
					JSONObject payload = new JSONObject();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("userId") == null
								|| jsonData.get("name") == null
								|| jsonData.get("address") == null
								|| jsonData.get("phone") == null) {

							responseData.put("result", false);
							responseData.put("description",
									"Please send all the user details");
						} else {
							Dbcon db = new Dbcon();

							String sql = "update tbl_student set name='"
									+ jsonData.get("name") + "' , address='"
									+ jsonData.get("address")
									+ "' , phone_no='" + jsonData.get("phone")
									+ "' where id = " + jsonData.get("userId");

							int update = db.update(sql);
							if (update <= 0) {
								responseData.put("result", false);
								responseData
										.put("description",
												"Could not update now, Please try again later");
							} else {
								
								sql = "select * from tbl_student where id=" + jsonData.get("userId");
								ResultSet rs = db.select(sql);
								
								if (rs.next()) {
									responseData.put("result", true);
									responseData.put("description",
											"Student details updated successfully");
									payload.put("name", rs.getString("name"));
									payload.put("userId", rs.getString("id"));
									payload.put("branch", rs.getString("branch"));
									payload.put("address", rs.getString("address"));
									payload.put("phone", rs.getString("phone_no"));
									payload.put("admissionDateMilli",
											rs.getString("admdate_milli"));
									payload.put("college", rs.getString("college"));
									responseData.put("payload", payload);
								} else {
									responseData.put("result", false);
									responseData.put("description",
											"Could not update now, Please try again later");
								}
								
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

	}
}