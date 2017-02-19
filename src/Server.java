import static spark.Spark.*;

import java.sql.ResultSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import spark.Spark;

public class Server {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Server Started............ ");

		staticFiles.externalLocation(Constants.external_file_location);

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
								payload.put("college_id",
										rs.getString("college_id"));
								payload.put("semester",
										rs.getString("semester"));
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

								sql = "select * from tbl_student where id="
										+ jsonData.get("userId");
								ResultSet rs = db.select(sql);

								if (rs.next()) {
									responseData.put("result", true);
									responseData
											.put("description",
													"Student details updated successfully");
									payload.put("name", rs.getString("name"));
									payload.put("userId", rs.getString("id"));
									payload.put("branch",
											rs.getString("branch"));
									payload.put("address",
											rs.getString("address"));
									payload.put("phone",
											rs.getString("phone_no"));
									payload.put("admissionDateMilli",
											rs.getString("admdate_milli"));
									payload.put("college",
											rs.getString("college"));
									responseData.put("payload", payload);
								} else {
									responseData.put("result", false);
									responseData
											.put("description",
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

		post("/sendFeedback",
				(request, response) -> {
					System.out.println("sendFeedback  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();
					JSONObject payload = new JSONObject();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("userId") == null
								|| jsonData.get("college_id") == null
								|| jsonData.get("feedbackMessage") == null) {

							responseData.put("result", false);
							responseData.put("description",
									"Please send all the details");
						} else {
							Dbcon db = new Dbcon();

							String sql = "insert into tbl_feedback (owner, audience, title, date , college_id) values('"
									+ jsonData.get("userId")
									+ "' , "
									+ " '"
									+ jsonData.get("college_id")
									+ "' , '"
									+ jsonData.get("feedbackMessage")
									+ "' , '"
									+ System.currentTimeMillis()
									+ "' , '"
									+ jsonData.get("college_id") + "' )";

							int ins = db.insert(sql);
							if (ins <= 0) {
								responseData.put("result", false);
								responseData
										.put("description",
												"Could not send feedback now, Please try again later");
							} else {
								responseData.put("result", true);
								responseData.put("description",
										"Feedback posted successfully");
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

		post("/getMainTimeTable",
				(request, response) -> {
					System.out.println("getMainTimeTable  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("semester") == null
								|| jsonData.get("branch") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send semester ");
						} else {
							JSONObject payload = new JSONObject();
							JSONArray dataarray = new JSONArray();
							Dbcon db = new Dbcon();

							String sql = "select * from tbl_time_table where semester='"
									+ jsonData.get("semester")
									+ "' and branch='"
									+ jsonData.get("branch")
									+ "'";
							ResultSet rs = db.select(sql);
							while (rs.next()) {
								JSONObject eachExam = new JSONObject();
								eachExam.put("revision_code",
										rs.getString("revision_code"));

								eachExam.put("subject_code",
										rs.getString("subject_code"));

								eachExam.put("subject_name",
										rs.getString("subject_name"));

								eachExam.put("date_milli",
										rs.getString("date_milli"));

								eachExam.put("time", rs.getString("time"));

								eachExam.put("date", rs.getString("date"));

								dataarray.add(eachExam);
							}
							responseData.put("result", true);
							responseData.put("description",
									"Sucessfully fetched ");
							responseData.put("payload", dataarray);
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

		post("/getSeriesTimeTable",
				(request, response) -> {
					System.out.println("getSeriesTimeTable  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("semester") == null
								|| jsonData.get("branch") == null
								|| jsonData.get("college_id") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send semester ");
						} else {
							JSONObject payload = new JSONObject();
							JSONArray dataarray = new JSONArray();
							Dbcon db = new Dbcon();

							String sql = "select * from tbl_series_time_table where semester='"
									+ jsonData.get("semester")
									+ "' and branch='"
									+ jsonData.get("branch")
									+ "' and college_id='"
									+ jsonData.get("college_id") + "'";
							ResultSet rs = db.select(sql);
							while (rs.next()) {
								JSONObject eachExam = new JSONObject();

								eachExam.put("subject_name",
										rs.getString("subject_name"));

								eachExam.put("date_milli",
										rs.getString("date_milli"));

								eachExam.put("time", rs.getString("time"));

								eachExam.put("date", rs.getString("date"));

								dataarray.add(eachExam);
							}
							responseData.put("result", true);
							responseData.put("description",
									"Sucessfully fetched ");
							responseData.put("payload", dataarray);
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